/*
 * Copyright (C) 2012 Danut Chereches
 *
 * Contact: Danut Chereches <admin@weednet.ro>
 *
 * This file is part of Facebook Contact Sync.
 * 
 * Facebook Contact Sync is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Facebook Contact Sync.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package uk.digitalsquid.remme.fbsync.client;

final public class ContactStreamItem {
	private final long mRawContactId;
	private final String mId;
	private final String mUid;
	private String mText;
	private String mPhotoUrl;
	private final long mTimestamp;
	
	public long getRawContactId() {
		return mRawContactId;
	}
	public String getId() {
		return mId;
	}
	public String getUid() {
		return mUid;
	}
	public String getText() {
		return mText;
	}
	public String getPhotoUrl() {
		return mPhotoUrl;
	}
	public long getTimestamp() {
		return mTimestamp;
	}
	
	public ContactStreamItem(RawContact contact, String id, String text, long timestamp) {
		this(contact, id, text, null, timestamp);
	}
	public ContactStreamItem(RawContact contact, String id, String text, String photoUrl, long timestamp) {
		mRawContactId = contact.getRawContactId();
		mUid = contact.getUid();
		mId = id;
		mText = text;
		mPhotoUrl = photoUrl;
		mTimestamp = timestamp;
	}
}
