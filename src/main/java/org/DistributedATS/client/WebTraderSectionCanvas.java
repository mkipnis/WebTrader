/*
   Copyright (C) 2021 Mike Kipnis

   This file is part of DistributedATS, a free-software/open-source project
   that integrates QuickFIX and LiquiBook over OpenDDS. This project simplifies
   the process of having multiple FIX gateways communicating with multiple
   matching engines in realtime.
   
   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
*/

package org.DistributedATS.client;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;

public class WebTraderSectionCanvas extends Canvas {
	
	public WebTraderSectionCanvas( String title )
	{
		setWidth100();
		setHeight100();
		this.title = title;
		setShowResizeBar(true);
	}
	
	public void setSectionStackCanvas( Canvas canvasIn )
	{
		  SectionStack webTraderSectionStack = new SectionStack();  
		  webTraderSectionStack.setWidth100();  
		  webTraderSectionStack.setHeight100();
		  webTraderSectionStack.setHeaderHeight(35);
			
	      SectionStackSection webTraderSection = new SectionStackSection("<b>"+title+"</b>");  
	      webTraderSection.setCanCollapse(false);  
	      webTraderSection.setExpanded(true);  
	      
	      canvasIn.setWidth100();
	      canvasIn.setHeight100();
	    
	      webTraderSection.addItem(canvasIn);
	      webTraderSectionStack.setSections(webTraderSection);
	      
	      addChild(webTraderSectionStack);
	}

	private String title;
}
