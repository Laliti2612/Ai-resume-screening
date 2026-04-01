package com.hrtech.resume_screening.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ResumeParserService {

    private final Tika tika = new Tika();

    public String parseResume(MultipartFile file) {

        try {

            String mimeType = tika.detect(file.getInputStream());
            log.info("Detected MIME type: {}", mimeType);

            if (!isSupportedType(mimeType)) {
                throw new RuntimeException("Unsupported file type: " + mimeType);
            }

            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            AutoDetectParser parser = new AutoDetectParser();

            parser.parse(
                    file.getInputStream(),
                    handler,
                    metadata,
                    context
            );

            String text = handler.toString();

            log.info("Extracted {} characters from resume", text.length());

            return cleanText(text);

        } catch (Exception e) {

            log.error("Resume parsing failed: {}", e.getMessage());

            throw new RuntimeException(e.getMessage());
        }
    }

    private String cleanText(String raw) {

        return raw
                .replaceAll("[^\\x20-\\x7E\\n]", " ")
                .replaceAll("\\s{3,}", "\n")
                .trim();
    }

    private boolean isSupportedType(String mimeType) {

        return mimeType.contains("pdf") ||
                mimeType.contains("word") ||
                mimeType.contains("text");
    }
}