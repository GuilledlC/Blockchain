package ledger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import users.Vote;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class Ledger {

    private static final String BLOCKS_DIRECTORY = "blocks/";

    public static void storeBlock(Block block) {
        ObjectMapper objectMapper = new ObjectMapper();
        File directory = new File(BLOCKS_DIRECTORY);

        // Create blocks directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Generate a file name based on block hash (you can adjust this according to your requirements)
        String fileName = BLOCKS_DIRECTORY + byteArrayToHexString(block.getHash()) + ".json";
        File file = new File(fileName);

        try {
            // Serialize block to JSON and write to file
            objectMapper.writeValue(file, block);
            System.out.println("Block serialized and stored: " + fileName);
        } catch (IOException e) {
            System.err.println("Error occurred while serializing block: " + e.getMessage());
        }
    }

    // Helper method to convert byte array to hexadecimal string
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    public Vote searchVote(Vote voteSearched) {
        final Vote[] result = new Vote[1];

        File directory = new File(BLOCKS_DIRECTORY);

        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        // Index for vote IDs
        Map<PublicKey, List<File>> voteIdIndex = new HashMap<>();

        // List all JSON files in the directory
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (files != null) {
            // Use parallel stream for processing JSON files in parallel
            ForkJoinPool forkJoinPool = new ForkJoinPool(8); // Adjust the number of threads as needed
            forkJoinPool.submit(() -> {
                for (File file : files) {
                    try {
                        // Create JsonParser for more memory-efficient processing
                        JsonFactory jsonFactory = new JsonFactory();
                        JsonParser jsonParser = jsonFactory.createParser(file);

                        // Deserialize JSON using streaming API
                        while (jsonParser.nextToken() != null) {
                            if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                                ObjectNode blockNode = objectMapper.readTree(jsonParser);
                                Block block = objectMapper.treeToValue(blockNode, Block.class);

                                // Index vote IDs
                                for (Vote vote : block.getVotes()) {
                                    PublicKey voteId = vote.getKey();
                                    voteIdIndex.computeIfAbsent(voteId, k -> new ArrayList<>()).add(file);
                                }

                                long timeToCheck = voteSearched.getTime();
                                if (block.isTimeBetweenVotes(timeToCheck)) {
                                    if (voteIdIndex.containsKey(voteSearched.getKey()) &&
                                            voteIdIndex.get(voteSearched.getKey()).contains(file)) {
                                        result[0] = objectMapper.readValue(jsonParser, Vote.class);
                                        System.out.println("File: " + file.getName() + " - Vote ID found in block.");
                                        break;
                                    }
                                }
                            }
                        }
                        jsonParser.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).join();
        } else {
            System.out.println("No JSON files found in the directory.");
        }
        return result[0];
    }
}
