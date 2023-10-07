package ledger;

import users.Transaction;
import java.util.ArrayList;
import java.util.Collection;

public class Block {

    private final ArrayList<Transaction> transactions;

    public Block() {
        this.transactions = new ArrayList<>();
    }

    public Block(Collection<Transaction> transactions) {
        this();
        this.transactions.addAll(transactions);
    }

    public String displayBlock() {
        String block = "\nNumber of transactions in block: " + transactions.size();
        for(Transaction t: transactions) {
            block += "\n" + t.displayTransactionShort();
        }
        return block;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

}
