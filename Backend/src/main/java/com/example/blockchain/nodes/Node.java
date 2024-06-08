package com.example.blockchain.nodes;

import com.example.blockchain.ledger.Block;
import com.example.blockchain.sockets.NetworkUser;
import com.example.blockchain.users.Vote;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Node extends NetworkUser {

    static final int CHECK_VOTE_DELAY_S = 5;
    static final int BLOCK_BUILD_TIME_S = 5;
    static final int MIN_VOTES_BLOCK = 2;

    private final ArrayList<Vote> votes;

    private final ArrayList<Block> blocks;

    public Node(String id) {
        super(id);
        votes = new ArrayList<>();
        blocks = new ArrayList<>();
        buildBlocks();
    }

    private void syncVotes() {
        if(isListening()) {
            for (Object o: listener.getObjects()) {
                if(o instanceof Vote t && !votes.contains(t))
                    votes.add(t);
            }
        }
    }

    protected ArrayList<Vote> getVotes() {
        syncVotes();
        return votes;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    protected void buildBlocks() {
        new Thread(() -> {
            while(votes.isEmpty()) {
                try {
                    TimeUnit.SECONDS.sleep(CHECK_VOTE_DELAY_S);
                } catch (InterruptedException ignored) {}
                syncVotes();
            }

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    while (votes.size() < MIN_VOTES_BLOCK) {
                        try {
                            TimeUnit.SECONDS.sleep(CHECK_VOTE_DELAY_S);
                        } catch (InterruptedException ignored) {}
                        syncVotes();
                    }
                    buildBlock();
                }
            }, BLOCK_BUILD_TIME_S * 1000, BLOCK_BUILD_TIME_S * 1000);
        }).start();
    }

    private void buildBlock() {
        syncVotes();
        //Block block = new Block(votes);
        //blocks.add(block);
        votes.clear();
    }

    /**another 'ask' to send data*/

    /**another to maybe handshake,*/

    /**another to get version info to make sure they are all same version/etc*/

    /**Connect to other nodes**/

    /**Get connected by other nodes**/

    /**Propagate nodes*/

    /**Compare Ledgers**/

    /**Build Block**/

    /**Add Block to Ledger**/
}
