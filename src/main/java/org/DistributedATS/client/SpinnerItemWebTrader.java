package org.DistributedATS.client;

import org.DistributedATS.shared.ConvertUtils;
import org.DistributedATS.shared.Instrument;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.FormItemCriterionGetter;
import com.smartgwt.client.widgets.form.FormItemValueFormatter;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.FormItemIcon;
import com.smartgwt.client.widgets.form.fields.NextValueHandler;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.EditorExitEvent;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyDownEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyDownHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;
import com.smartgwt.client.widgets.form.validator.CustomValidator;
import com.smartgwt.client.widgets.form.validator.IsStringValidator;
import com.smartgwt.client.widgets.form.fields.events.EditorExitHandler;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;

import net.sf.saxon.value.DoubleValue;

import com.smartgwt.client.widgets.form.FormItemValueParser;


//
//
// TODO: Design control that deals with UST fractions
//
//
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
