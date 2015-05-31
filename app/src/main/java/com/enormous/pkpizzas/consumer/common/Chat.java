package com.enormous.pkpizzas.consumer.common;

/**
 * User: greg
 * Date: 6/21/13
 * Time: 2:38 PM
 */
public class Chat {

	private String id;
	private String message;
	private String author;
	private long time;

	// Required default constructor for Firebase object mapping
	@SuppressWarnings("unused")
	private Chat() { }

	public Chat(String id,String message, String author,long time) {
		this.message = message;
		this.author = author;
		this.id = id;
		this.time = time;
	}
	
	public long getTime(){
		return time;
	}

	public String getID(){
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String getAuthor() {
		return author;
	}
}
