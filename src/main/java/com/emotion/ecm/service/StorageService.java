package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsPrefixDao;
import com.emotion.ecm.model.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private SmsPrefixService smsPrefixService;
    private AppUserService userService;

    @Autowired
    public StorageService(SmsPrefixService smsPrefixService, AppUserService userService) {
        this.smsPrefixService = smsPrefixService;
        this.userService = userService;
    }

    public Map<String, Integer> storeAccountData(Path path, MultipartFile file) throws IllegalArgumentException, IOException {

        if (file.isEmpty() || path == null) {
            throw new IllegalArgumentException();
        }

        Map<String, Integer> result = new HashMap<>();

        AppUser user = userService.getAuthenticatedUser();

        BufferedWriter writer = Files.newBufferedWriter(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));

        Set<String> readLines = new HashSet<>();
        Set<String> invalidLines = new HashSet<>();
        String currLine;
        while ((currLine = reader.readLine()) != null) {
            if (!smsPrefixService.isMsisdnValidForAccount(user.getAccount(), currLine)) {
                invalidLines.add(currLine);
                continue;
            }
            if (!readLines.contains(currLine)) {
                writer.write(currLine);
                writer.newLine();
                readLines.add(currLine);
            }
        }

        result.put("valid", readLines.size());
        result.put("invalid", invalidLines.size());

        reader.close();
        writer.close();
        readLines.clear();

        LOGGER.info(String.format("%s was stored", path));

        return result;
    }

    public Path load(String fileName) {
        return null;
    }

    public Resource loadAsResource(String fileName) {
        return null;
    }

    public void delete(String fileName) {

    }

}
