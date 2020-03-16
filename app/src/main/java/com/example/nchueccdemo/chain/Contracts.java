package com.example.nchueccdemo.chain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.http.HttpService;

import java.io.Serializable;
import java.math.BigInteger;
/*
** 此java檔請勿修改參數
 */
public class Contracts implements Serializable {
    private Admin admin;
    private String userAddr, storeAddr, nchuAddr, ownerAddr, contractAddr;
    private String userPrKey, storePrKey, nchuPrKey, ownerPrKey;
    private Credentials userCredentials, storeCredentials, nchuCredentials, ownerCredentials;
    private NCHUToken userToken, storeToken, nchuToken, ownerToken;

    private BigInteger GAS_PRICE;
    private BigInteger GAS_LIMIT;

    public Contracts() {
        //
        admin = Admin.build(new HttpService("https://ropsten.infura.io/v3/..."));
        contractAddr = "0x...";

        GAS_PRICE = BigInteger.valueOf(15_000_000_000L); // 15_000_000_000L
        GAS_LIMIT = BigInteger.valueOf(4_300_000L);      // 4_300_000L

        setUser();
        setStore();
        setNCHU();
        setOwner();


    }

    private void setUser() {
        // address :
        userPrKey = "";

        userCredentials = Credentials.create(userPrKey);
        userAddr = userCredentials.getAddress();

        userToken = NCHUToken.load(
                contractAddr,
                admin,
                userCredentials,
                GAS_PRICE,
                GAS_LIMIT);
    }

    private void setStore() {
        // address :
        storePrKey = "";

        storeCredentials = Credentials.create(storePrKey);
        storeAddr = storeCredentials.getAddress();

        storeToken = NCHUToken.load(
                contractAddr,
                admin,
                storeCredentials,
                GAS_PRICE,
                GAS_LIMIT);
    }

    private void setNCHU() {
        // address :
        nchuPrKey = "";

        nchuCredentials = Credentials.create(nchuPrKey);
        nchuAddr = nchuCredentials.getAddress();

        nchuToken = NCHUToken.load(
                contractAddr,
                admin,
                nchuCredentials,
                GAS_PRICE,
                GAS_LIMIT);
    }

    public void setOwner() {
        // address :
        ownerPrKey = "";

        ownerCredentials = Credentials.create(ownerPrKey);
        ownerAddr = ownerCredentials.getAddress();

        ownerToken = NCHUToken.load(
                contractAddr,
                admin,
                ownerCredentials,
                GAS_PRICE,
                GAS_LIMIT);
    }

    public String getUserAddr() {
        return userAddr;
    }

    public String getStoreAddr() {
        return storeAddr;
    }

    public String getNchuAddr() {
        return nchuAddr;
    }

    public String getOwnerAddr() {
        return ownerAddr;
    }

    public NCHUToken getUserToken() {
        return userToken;
    }

    public NCHUToken getStoreToken() {
        return storeToken;
    }

    public NCHUToken getNchuToken() {
        return nchuToken;
    }

    public NCHUToken getOwnerToken() {
        return ownerToken;
    }

    public Admin getAdmin() { return admin; }

}
