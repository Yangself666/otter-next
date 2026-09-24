/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.common.utils.compile.model;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;

import com.alibaba.otter.shared.common.utils.compile.impl.JdkCompileTask;

public class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

    private final JdkCompilerClassLoader   classLoader;

    private final Map<URI, JavaFileObject> fileObjects = new HashMap<URI, JavaFileObject>();
    private final Map<String, ClassPathJavaFileObject> classPathFiles = new LinkedHashMap<>();
    private final Map<String, List<JavaFileObject>> classPathPackages = new HashMap<>();
    private final List<JarFile> openedJars = new ArrayList<>();
    private boolean classPathLoaded;

    public JavaFileManagerImpl(JavaFileManager fileManager, JdkCompilerClassLoader classLoader){
        super(fileManager);
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        FileObject o = fileObjects.get(uri(location, packageName, relativeName));

        if (o != null) {
            return o;
        }

        return super.getFileForInput(location, packageName, relativeName);
    }

    public void putFileForInput(StandardLocation location, String packageName, String relativeName, JavaFileObject file) {
        fileObjects.put(uri(location, packageName, relativeName), file);
    }

    private URI uri(Location location, String packageName, String relativeName) {
        return JdkCompileTask.toURI(location.getName() + '/' + packageName + '/' + relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind, FileObject outputFile)
                                                                                                                         throws IOException {
        JavaFileObject file = new JavaFileObjectImpl(qualifiedName, kind);
        classLoader.add(qualifiedName, file);
        return file;
    }

    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return classLoader;
    }

    @Override
    public String inferBinaryName(Location loc, JavaFileObject file) {
        if (file instanceof ClassPathJavaFileObject dependency) {
            return dependency.getBinaryName();
        }
        if (file instanceof JavaFileObjectImpl) {
            return file.getName();
        }

        return super.inferBinaryName(loc, file);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
                                                                                                                 throws IOException {
        Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);

        ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();

        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            loadClassPath();
            files.addAll(classPathPackages.getOrDefault(packageName, List.of()));
            if (recurse) {
                String prefix = packageName.isEmpty() ? "" : packageName + ".";
                for (var entry : classPathPackages.entrySet()) {
                    if (!entry.getKey().equals(packageName) && entry.getKey().startsWith(prefix)) {
                        files.addAll(entry.getValue());
                    }
                }
            }
            for (JavaFileObject file : fileObjects.values()) {
                if (file.getKind() == Kind.CLASS && file.getName().startsWith(packageName)) {
                    files.add(file);
                }
            }

            files.addAll(classLoader.files());
        } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
            for (JavaFileObject file : fileObjects.values()) {
                if (file.getKind() == Kind.SOURCE && file.getName().startsWith(packageName)) {
                    files.add(file);
                }
            }
        }

        for (JavaFileObject file : result) {
            files.add(file);
        }

        return files;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        JavaFileObject file = super.getJavaFileForInput(location, className, kind);
        if (file == null && location == StandardLocation.CLASS_PATH && kind == Kind.CLASS) {
            loadClassPath();
            return classPathFiles.get(className);
        }
        return file;
    }

    private void loadClassPath() throws IOException {
        if (classPathLoaded) return;
        // 使用启动类加载器的 JAR 连接，编译过程直接读取依赖而不解压到磁盘
        for (ClassLoader loader = classLoader.getParent(); loader != null; loader = loader.getParent()) {
            if (!(loader instanceof URLClassLoader urls)) continue;
            for (URL url : urls.getURLs()) {
                if (!"jar".equals(url.getProtocol())) continue;
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                connection.setUseCaches(false);
                JarFile jar = connection.getJarFile();
                openedJars.add(jar);
                String prefix = connection.getEntryName() == null ? "" : connection.getEntryName();
                if (!prefix.isEmpty() && !prefix.endsWith("/")) prefix += "/";
                var entries = jar.versionedStream().iterator();
                while (entries.hasNext()) {
                    var entry = entries.next();
                    String name = entry.getName();
                    if (!name.startsWith(prefix) || !name.endsWith(".class")) continue;
                    String relative = name.substring(prefix.length());
                    if (relative.startsWith("META-INF/") || relative.equals("module-info.class")) continue;
                    String binaryName = relative.substring(0, relative.length() - 6).replace('/', '.');
                    var file = new ClassPathJavaFileObject(binaryName, jar, entry);
                    if (classPathFiles.putIfAbsent(binaryName, file) == null) {
                        int dot = binaryName.lastIndexOf('.');
                        String packageName = dot < 0 ? "" : binaryName.substring(0, dot);
                        classPathPackages.computeIfAbsent(packageName, key -> new ArrayList<>()).add(file);
                    }
                }
            }
        }
        classPathLoaded = true;
    }

    @Override
    public void close() throws IOException {
        IOException failure = null;
        for (JarFile jar : openedJars) {
            try {
                jar.close();
            } catch (IOException e) {
                if (failure == null) failure = e;
                else failure.addSuppressed(e);
            }
        }
        try {
            super.close();
        } catch (IOException e) {
            if (failure == null) failure = e;
            else failure.addSuppressed(e);
        }
        if (failure != null) throw failure;
    }
}
