package com.banking.document.service;

import java.util.List;

public interface ChunkingService {

    List<String> chunkText(String text, int chunkSize, int overlap);

    int estimateTokenCount(String text);
}
