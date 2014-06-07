package com.openpeer.javaapi;

import java.util.List;

public class OPRolodexContact {

	public class OPAvatar {
		private String mName;
		private String mURL;
		private int mWidth;
		private int mHeight;
		
		public OPAvatar(String mName, String mURL, int mWidth, int mHeight) {
			super();
			this.mName = mName;
			this.mURL = mURL;
			this.mWidth = mWidth;
			this.mHeight = mHeight;
		}

		OPAvatar()
		{
			
		}
		
		public String getName() {
			return mName;
		}
		public void setName(String mName) {
			this.mName = mName;
		}
		public String getURL() {
			return mURL;
		}
		public void setURL(String mURL) {
			this.mURL = mURL;
		}
		public int getWidth() {
			return mWidth;
		}
		public void setWidth(int mWidth) {
			this.mWidth = mWidth;
		}
		public int getHeight() {
			return mHeight;
		}
		public void setHeight(int mHeight) {
			this.mHeight = mHeight;
		}
		public String toString(){
			return super.toString() + " Name "+mName+" URL "+mURL+" Width "+ mWidth + " Height "+mHeight;
		}
		
	};
	
	public enum Dispositions
    {
      Disposition_NA,
      Disposition_Update,
      Disposition_Remove;
      
      Dispositions ()
      {
      }

    };
    
    private Dispositions mDisposition;
    private String mIdentityURI;
    private String mIdentityProvider;

    private String mName;
    private String mProfileURL;
    private String mVProfileURL;

    private List<OPAvatar> mAvatars;

    public OPRolodexContact(Dispositions mDisposition, String mIdentityURI,
			String mIdentityProvider, String mName, String mProfileURL,
			String mVProfileURL, List<OPAvatar> mAvatars) {
		super();
		this.mDisposition = mDisposition;
		this.mIdentityURI = mIdentityURI;
		this.mIdentityProvider = mIdentityProvider;
		this.mName = mName;
		this.mProfileURL = mProfileURL;
		this.mVProfileURL = mVProfileURL;
		this.mAvatars = mAvatars;
	}
	public OPRolodexContact(){
    	
    }
	public Dispositions getDisposition() {
		return mDisposition;
	}

	public void setDisposition(Dispositions mDisposition) {
		this.mDisposition = mDisposition;
	}

	public String getIdentityURI() {
		return mIdentityURI;
	}

	public void setIdentityURI(String mIdentityURI) {
		this.mIdentityURI = mIdentityURI;
	}

	public String getIdentityProvider() {
		return mIdentityProvider;
	}

	public void setIdentityProvider(String mIdentityProvider) {
		this.mIdentityProvider = mIdentityProvider;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getProfileURL() {
		return mProfileURL;
	}

	public void setProfileURL(String mProfileURL) {
		this.mProfileURL = mProfileURL;
	}

	public String getVProfileURL() {
		return mVProfileURL;
	}

	public void setVProfileURL(String mVProfileURL) {
		this.mVProfileURL = mVProfileURL;
	}

	public List<OPAvatar> getAvatars() {
		return mAvatars;
	}

	public void setAvatars(List<OPAvatar> mAvatars) {
		this.mAvatars = mAvatars;
	}
	
	void copy(OPRolodexContact contact){
		this.mAvatars = contact.mAvatars;
		this.mDisposition = contact.mDisposition;
		this.mIdentityProvider = contact.mIdentityProvider;
		this.mIdentityURI = contact.mIdentityURI;
		this.mName = contact.mName;
		this.mProfileURL = contact.mProfileURL;
		this.mVProfileURL = contact.mVProfileURL;
	}
	
	public String toString(){
		return super.toString() + " ProfileURL "+mProfileURL + " Name "+mName+" identityUrl "+mIdentityURI
				+ " IdentityProvider " + mIdentityProvider + " Disposition " + mDisposition;
	}
}
