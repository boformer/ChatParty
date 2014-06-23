/*
 ChatParty Plugin for Minecraft Bukkit Servers
 This file: Copyright (C) 2014 Dr Daniel Naylor
    
 This file is part of ChatParty.

 ChatParty is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 ChatParty is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with ChatParty.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.drnaylor.chatparty.exceptions;

/**
 * Represents an exception in ChatParty.
 */
public class ChatPartyException extends Exception {
    
    private final String message;
    
    /**
     * Initializes the ChatPartyException class.
     * 
     * @param message The message to associate with the exception.
     */
    public ChatPartyException(String message) {
        this.message = message;
    }
    
    /**
     * Get the message from the exception.
     * 
     * @return The message.
     */
    @Override
    public String getMessage() {
        return message;
    }
    
}
