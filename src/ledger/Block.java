package ledger;

import users.Vote;
import java.util.ArrayList;
import java.util.Collection;

public class Block {

    private final ArrayList<Vote> votes;

    public Block() {
        this.votes = new ArrayList<>();
    }

    public Block(Collection<Vote> votes) {
        this();
        this.votes.addAll(votes);
    }

    public String displayBlock() {
        String block = "\nNumber of votes in block: " + votes.size();
        for(Vote t: votes) {
            block += "\n" + t.displayVoteShort();
        }
        return block;
    }

    public ArrayList<Vote> getVotes() {
        return votes;
    }

}
