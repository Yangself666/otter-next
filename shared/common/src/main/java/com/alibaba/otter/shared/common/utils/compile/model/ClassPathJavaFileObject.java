package com.alibaba.otter.shared.common.utils.compile.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.tools.SimpleJavaFileObject;

/**
 * 通过 JAR 条目向编译器提供依赖字节码，支持可执行 JAR 中的嵌套依赖
 */
final class ClassPathJavaFileObject extends SimpleJavaFileObject {

    private final String binaryName;
    private final JarFile jar;
    private final JarEntry entry;

    ClassPathJavaFileObject(String binaryName, JarFile jar, JarEntry entry) {
        super(URI.create("classpath:///" + binaryName.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
        this.binaryName = binaryName;
        this.jar = jar;
        this.entry = entry;
    }

    String getBinaryName() {
        return binaryName;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return jar.getInputStream(entry);
    }
}
