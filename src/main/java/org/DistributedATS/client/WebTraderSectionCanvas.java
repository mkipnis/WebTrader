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
