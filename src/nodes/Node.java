package nodes;

import sockets.NetworkUser;
import users.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class Node extends NetworkUser {

    private ArrayList<Transaction> transactions;

    public Node(String id) {
        super(id);
        transactions = new ArrayList<>();
    }

    private void syncTransactions() {
        for (Object o: getObjects()) {
            if(o instanceof Transaction t && !transactions.contains(t))
                transactions.add(t);
        }
    }

    public ArrayList<Transaction> getTransactions() {
        syncTransactions();
        return transactions;
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
