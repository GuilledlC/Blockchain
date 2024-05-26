package nodes;

import ledger.Block;
import sockets.NetworkUser;
import users.Vote;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.net.InetAddress;
import java.util.Random;

public class Node extends NetworkUser {

    static final int CHECK_VOTE_DELAY_S = 5;
    static final int BLOCK_BUILD_TIME_S = 5;
    static final int MIN_VOTES_BLOCK = 2;
    private static String ip = null;
    private static String actualminer = null;
    private static ArrayList<Object[]> nonminedblocks = new ArrayList<>();
    private final ArrayList<Vote> votes;
    private final ArrayList<Block> blocks;

    public Node(String ip) {
        super(ip);
        this.ip = ip;
        votes = new ArrayList<>();
        blocks = new ArrayList<>();
        buildBlocks();
        //setNonMinedBlocks("192.168.0.1");         //Add as much as IPs your network has
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
        Block block = new Block(votes);
        blocks.add(block);
        votes.clear();
    }

    private Block recieveBlockFromMiner(String ip){
        //Not done yet
        Block block = null;
        return block;
    }

    private void setNonMinedBlocks(String ip){
        nonminedblocks.add(new Object[]{ip, 1});
    }

    private static void addEveryoneExceptTo(String ip){
        for (Object[] item : nonminedblocks){
            if ((int)item[1] != 0) {
                item[1] = (double) item[1] + 1;
            }
            if (item[0].equals(ip)){
                item[1] = (double)item[1] - 1;
            }
        }
    }

    private boolean correctBlock (Block block) {
        boolean aux = true;
        while (aux) {
            //checkBlock
        }
        return aux;
    }

    private void punishNode(String ip){
        for (Object[] item : nonminedblocks){
            if (item[0].equals(ip)){
                item[1] = 0;
            }
        }
    }

    private int getNonMinedBlocksModule(){
        int aux = 0;
        for (Object[] item : nonminedblocks){
            aux += (int)item[1];
        }
        return aux;
    }

    private int moduleOf(int number, int module){
        return number % module;
    }

    private void proofOfConsensus(int magicnumber){
        int aux = -1;
        String miner = null;
        for (Object[] item : nonminedblocks){
            aux += (int)item[1];
            miner = (String)item[0];
            if (aux >= magicnumber){
                actualminer = miner;
                break;
            }
        }
    }

    private boolean mining(){
        return ip.equals(actualminer);
    }

    private void nodeExecution(){
        Block minedblock;

        if (actualminer != null){
            if (mining()){
                minedblock = new Block(getVotes());
                blocks.add(minedblock);
                //delete votes and update voted users
            }
            else{
                minedblock = recieveBlockFromMiner(actualminer);
                if (correctBlock(minedblock)){blocks.add(minedblock);}
                else {punishNode(actualminer);}
            }
        }
        Random random = new Random();
        int randomnumber = random.nextInt(getNonMinedBlocksModule());
        //send randomnumber
        //recieve random numbers from nodes and increase randomnumber
        proofOfConsensus(moduleOf(randomnumber, getNonMinedBlocksModule()));
        addEveryoneExceptTo(actualminer);
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
