package com.alibaba.otter.node.deployer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DownloadController {

    private final Path root;

    public DownloadController(@Value("${otter.htdocs.dir}") String directory) throws IOException {
        root = Files.createDirectories(Path.of(directory)).toRealPath();
    }

    @GetMapping("/download/{*path}")
    public ResponseEntity<Resource> download(@PathVariable String path) throws IOException {
        Path file = root.resolve(path.replaceFirst("^/+", "")).normalize();
        if (!file.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!Files.exists(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // 校验真实路径，保证符号链接和路径参数都位于下载目录内
        file = file.toRealPath();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .lastModified(Files.getLastModifiedTime(file).toMillis()).body(new FileSystemResource(file));
    }
}
