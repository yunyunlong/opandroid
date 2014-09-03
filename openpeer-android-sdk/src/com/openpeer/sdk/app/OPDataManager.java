/*******************************************************************************
 *
 *  Copyright (c) 2014 , Hookflash Inc.
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are those
 *  of the authors and should not be interpreted as representing official policies,
 *  either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/
package com.openpeer.sdk.app;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.util.Log;

import com.openpeer.javaapi.AccountStates;
import com.openpeer.javaapi.OPAccount;
import com.openpeer.javaapi.OPCall;
import com.openpeer.javaapi.OPContact;
import com.openpeer.javaapi.OPConversationThread;
import com.openpeer.javaapi.OPDownloadedRolodexContacts;
import com.openpeer.javaapi.OPIdentity;
import com.openpeer.javaapi.OPIdentityContact;
import com.openpeer.javaapi.OPIdentityLookup;
import com.openpeer.javaapi.OPIdentityLookupInfo;
import com.openpeer.javaapi.OPLogLevel;
import com.openpeer.javaapi.OPLogger;
import com.openpeer.javaapi.OPRolodexContact;
import com.openpeer.sdk.datastore.OPDatastoreDelegate;
import com.openpeer.sdk.delegates.OPIdentityLookupDelegateImpl;
import com.openpeer.sdk.model.OPUser;

/**
 * Hold reference to objects that cannot be constructed from database, and manages contacts data change.
 * 
 */
public class OPDataManager {
    private static final String TAG = OPDataManager.class.getSimpleName();

    public static String INTENT_CONTACTS_CHANGED = "com.openpeer.contacts_changed";

    private static OPDataManager instance;
    private OPDatastoreDelegate mDatastoreDelegate;

    private OPAccount mAccount;
    private List<OPIdentity> mIdentities;
    private List<OPIdentityContact> mSelfContacts;
    Hashtable<String, OPIdentityLookup> mIdentityLookups = new Hashtable<String, OPIdentityLookup>();

    private boolean mAccountReady;

    public static OPDatastoreDelegate getDatastoreDelegate() {
        return getInstance().mDatastoreDelegate;
    }

    public List<OPIdentity> getIdentities() {
        return mIdentities;
    }

    public String getReloginInfo() {
        return mDatastoreDelegate.getReloginInfo();
    }

    public static OPDataManager getInstance() {
        if (instance == null) {
            instance = new OPDataManager();
        }
        return instance;
    }

    public void init(OPDatastoreDelegate delegate) {
        assert (delegate != null);
        mDatastoreDelegate = delegate;
        // mContacts = new Hashtable<Long, List<OPRolodexContact>>();
        // if (mReloginInfo != null) {
        // // Read idenities contacts and contacts
        // mSelfContacts = mDatastoreDelegate.getSelfIdentityContacts();
        // }
    }

    /**
     * This function should only be called in AccountState_Ready from OPAccountDelegate. This function update the database
     * 
     * @param account
     *            the logged in account
     */
    public void setSharedAccount(OPAccount account) {
        mAccount = account;
    }

    public void saveAccount() {
        mDatastoreDelegate.saveOrUpdateAccount(mAccount);
    }

    public OPAccount getSharedAccount() {
        return mAccount;
    }

    public void setIdentities(List<OPIdentity> identities) {
        mIdentities = identities;
        mSelfContacts = new ArrayList<OPIdentityContact>();
        for (OPIdentity identity : identities) {
            mSelfContacts.add(identity.getSelfIdentityContact());
        }
        mDatastoreDelegate.saveOrUpdateIdentities(mIdentities, mAccount.getID());
    }

    public List<OPIdentityContact> getSelfContacts() {
        return mSelfContacts;
    }

    public void onDownloadedRolodexContacts(OPIdentity identity) {
        OPDownloadedRolodexContacts downloaded = identity.getDownloadedRolodexContacts();
        long identityId = identity.getStableID();
        String contactsVersion = downloaded.getVersionDownloaded();

        List<OPRolodexContact> contacts = downloaded.getRolodexContacts();

        for (OPRolodexContact contact : contacts) {
            switch (contact.getDisposition()) {
            case Disposition_Remove:
                mDatastoreDelegate.deleteContact(contact.getIdentityURI());
                break;
            case Disposition_Update:
                // break;
            default:
                mDatastoreDelegate.saveOrUpdateContact(contact, identityId);
            }
        }
        mDatastoreDelegate.notifyContactsChanged();
        identityLookup(identity, contacts);
    }

    public void identityLookup(OPIdentity identity, List<OPRolodexContact> contacts) {

        OPIdentityLookupDelegateImpl mIdentityLookupDelegate = OPIdentityLookupDelegateImpl.getInstance(identity);
        List<OPIdentityLookupInfo> inputLookupList = new ArrayList<OPIdentityLookupInfo>();

        for (OPRolodexContact contact : contacts) {
            OPIdentityLookupInfo ilInfo = new OPIdentityLookupInfo();
            ilInfo.initWithRolodexContact(contact);
            inputLookupList.add(ilInfo);
        }

        OPIdentityLookup identityLookup = OPIdentityLookup.create(OPDataManager.getInstance().getSharedAccount(), mIdentityLookupDelegate,
                inputLookupList, OPSdkConfig.getInstance().getIdentityProviderDomain());// "identity-v1-rel-lespaul-i.hcs.io");
        if (identityLookup != null) {
            mIdentityLookups.put(identity.getIdentityURI(), identityLookup);
        }
    }

    public String getContactsVersionForIdentity(long id) {
        return mDatastoreDelegate.getDownloadedContactsVersion(id);
    }

    public void updateIdentityContacts(String identityUri, List<OPIdentityContact> iContacts) {

        // Each IdentityContact represents a user. Update user info
        mDatastoreDelegate.saveOrUpdateUsers(iContacts, identityUri.hashCode());
    }

    public void refreshContacts() {
        List<OPIdentity> identities = mAccount.getAssociatedIdentities();
        for (OPIdentity identity : identities) {

            identity.refreshRolodexContacts();
        }
    }

    public long getUserIdForContact(OPContact contact,
            OPIdentityContact iContact) {
        // TODO implement proper userId querying and gereration
        return contact.getPeerURI().hashCode();
    }

    public boolean isAccountReady() {
        return mAccount != null
                && mAccount.getState() == AccountStates.AccountState_Ready;
    }

    public OPUser getUserByPeerUri(String uri) {
        return mDatastoreDelegate.getUserByPeerUri(uri);
    }

    public OPUser getPeerUserForCall(OPCall call) {
        OPContact contact = call.getPeer();
        OPUser user = mDatastoreDelegate.getUserByPeerUri(contact.getPeerURI());
        if (user == null) {
            List<OPIdentityContact> identityContacts = call.getIdentityContactList(contact);
            if (identityContacts == null || identityContacts.isEmpty()) {
                OPLogger.error(OPLogLevel.LogLevel_Basic,
                        "getIdentityContactList returns empty in call for contact " + contact.getPeerURI());
            }
            user = new OPUser(contact, identityContacts);
            user = mDatastoreDelegate.saveUser(user);
        }
        return user;
    }

    /**
     * @param url
     * @param lookup
     */
    public void onIdentityLookupCompleted(String url, OPIdentityLookup lookup) {
        List<OPIdentityContact> iContacts = lookup.getUpdatedIdentities();
        if (iContacts != null) {
            updateIdentityContacts(url, iContacts);
        }
        mIdentityLookups.remove(url);
    }

    /**
     * @param from
     * @param opConversationThread
     * @return
     */
    public OPUser getUserForMessage(OPContact from, OPConversationThread thread) {
        OPUser user = getUserByPeerUri(from.getPeerURI());
        if (user == null) {
            user = new OPUser(from, thread.getIdentityContactList(from));
            user = mDatastoreDelegate.saveUser(user);
        }
        return user;
    }

    public OPUser getUserById(long id) {
        return mDatastoreDelegate.getUserById(id);
    }

    public void onSignOut() {
        List<OPIdentity> identities = instance.mAccount.getAssociatedIdentities();
        if (mIdentityLookups != null && mIdentityLookups.size() > 0) {
            for (OPIdentityLookup lookup : mIdentityLookups.values()) {
                lookup.cancel();
            }
        }
        for (OPIdentity identity : identities) {
            identity.cancel();
        }
        mIdentities = null;
        mSelfContacts = null;
        mAccount.shutdown();
        mAccount = null;
        mDatastoreDelegate.onSignOut();

    }

}
