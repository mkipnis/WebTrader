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

import org.DistributedATS.shared.ConvertUtils;
import org.DistributedATS.shared.Instrument;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.FormItemValueFormatter;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyDownEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyDownHandler;


public class SpinnerItemWebTrader extends SpinnerItem {
	
	public Integer current_value;
	
	public Instrument m_instrument_with_ref_data;
	
	public SpinnerItemWebTrader()
	{		
		this.addKeyDownHandler( new KeyDownHandler() 
		{

			@Override
			public void onKeyDown(KeyDownEvent event) {
				// TODO Auto-generated method stub
				
				if ( event.getKeyName().equals("Tab") || event.getKeyName().equals("Enter") )
				{
					String price_text = getEnteredValue().toString();
					
					current_value = ConvertUtils.getTicksFromDisplayPrice(m_instrument_with_ref_data, price_text);
				
					setValue(current_value);
				}
			}
			
		});
		
		
		setEditorValueFormatter(new FormItemValueFormatter (){
			@Override
			public String formatValue(Object value, Record record, DynamicForm form, FormItem item) {
			
					return ConvertUtils.getDisplayPrice(m_instrument_with_ref_data, (int)current_value);
			
			}
		});
	}
	
	
	public void setValue(final Integer value, final Instrument instrument_with_ref_data)
	{			
		
		m_instrument_with_ref_data = instrument_with_ref_data;			
	    setValue( current_value = value );
	    
	    if ( instrument_with_ref_data.getTickSize() % 256 == 0 )
	    {
	    	setTitle("Price(32nd)");
	    	this.updateState();
	    } else {
	    	setTitle("Price");
	    	this.updateState();
	    }
	    
		getDecreaseIcon().addFormItemClickHandler( new FormItemClickHandler()
		{

			@Override
			public void onFormItemClick(FormItemIconClickEvent event) {
					// TODO Auto-generated method stub
					current_value -= 1;
					setValue( current_value );
				}
		});
		

		getIncreaseIcon().addFormItemClickHandler( new FormItemClickHandler()
		{

			@Override
			public void onFormItemClick(FormItemIconClickEvent event) {
					// TODO Auto-generated method stub
					current_value += 1;
					setValue( current_value );
				}

		});
	    
	}
	
	public Integer getValueInTicks()
	{
		return current_value;
	}
	

}
