
package com.ripple.android.profile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties({
        "preferred_second_issuer", "preferred_issuer"
})
public class User implements Serializable {
    private static final long serialVersionUID = 590287486343419861L;

    @JsonProperty("account_id")
    private String walletAddress;

    @JsonProperty("master_seed")
    private String masterSeed;

    private List<Contact> contacts;

    private BigDecimal balance;

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public String getMasterSeed() {
        return masterSeed;
    }

    public void setMasterSeed(String masterSeed) {
        this.masterSeed = masterSeed;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;

    }

}
