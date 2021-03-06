package edu.berkeley.calcentral.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

/**
 * A CalCentral user, containing basic information that we can save in our local database.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class User {

	private String uid;

	private String preferredName;

	private String link;

	private String profileImageLink;

	private Timestamp firstLogin;

	public User() {
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Timestamp getFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(Timestamp firstLogin) {
		this.firstLogin = firstLogin;
	}

	public String getProfileImageLink() {
		return profileImageLink;
	}

	public void setProfileImageLink(String profileImageLink) {
		this.profileImageLink = profileImageLink;
	}

	@Override
	public String toString() {
		return "User{" +
				"uid='" + uid + '\'' +
				", preferredName='" + preferredName + '\'' +
				", link='" + link + '\'' +
				", profileImageLink='" + profileImageLink + '\'' +
				", firstLogin=" + firstLogin +
				'}';
	}
}
