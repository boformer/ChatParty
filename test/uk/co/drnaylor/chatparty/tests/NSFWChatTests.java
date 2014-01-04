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

package uk.co.drnaylor.chatparty.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.co.drnaylor.chatparty.nsfw.NSFWChat;
import uk.co.drnaylor.chatparty.tests.stubs.ChatPartyPluginStub;

@RunWith(Parameterized.class)
public class NSFWChatTests {
    
    private NSFWChat instance;  
    
    private String testString;
    private boolean expectedResult;
    
    public NSFWChatTests(String testString, boolean expectedResult) {
        this.testString = testString;
        this.expectedResult = expectedResult;
    }
    
   @Parameterized.Parameters
   public static Collection params() {
      return Arrays.asList(new Object[][] {
         { "This is a test string", true },
         { "This is not", false },
         { "This should trigger the te.s*t.", true },
         { "This should trigger the Test.", true },
         { "This should trigger the TeSt.", true },
         { "Thi-s sh-34 te9sge", false },
         { "This is a stests", false }
      });
   }

    
    @Before
    public void setUp() {
        // Create a new instance of the NSFWChat class.
        instance = new NSFWChat(new ChatPartyPluginStub());
        ArrayList<String> bw = new ArrayList<String>();
        bw.add("test");
        instance.setupFilter(bw);
    }
    

    @Test
    public void testBannedWords() {
        Assert.assertEquals(expectedResult, instance.containsBannedWord(testString));   
    }
}
