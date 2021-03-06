package org.DistributedATS.client;

import org.DistributedATS.shared.ConvertUtils;
import org.DistributedATS.shared.Instrument;

import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.data.Criterion;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.types.TextMatchStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.FormItemCriterionGetter;
import com.smartgwt.client.widgets.form.FormItemValueFormatter;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.NextValueHandler;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyDownEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyDownHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;

import net.sf.saxon.value.DoubleValue;

import com.smartgwt.client.widgets.form.FormItemValueParser;

public class SpinnerItemWebTrader extends SpinnerItem {
	
	//public double current_value;
	
	public Instrument m_instrument_with_ref_data;
	
	
	public void setValue(Integer value, Instrument instrument_with_ref_data)
	{
	/*	getDecreaseIcon().addFormItemClickHandler( new FormItemClickHandler()
		{

			@Override
			public void onFormItemClick(FormItemIconClickEvent event) {
				// TODO Auto-generated method stub
				current_value -= 1;
				
				setValue( ConvertUtils.formatPrice32((int)current_value) );
			}
	
		}
		);
		

		getIncreaseIcon().addFormItemClickHandler( new FormItemClickHandler()
{

	@Override
	public void onFormItemClick(FormItemIconClickEvent event) {
		// TODO Auto-generated method stub
		current_value += 1;
		
		setValue( ConvertUtils.formatPrice32((int)current_value) );
	}

}
);*/
		
		m_instrument_with_ref_data = instrument_with_ref_data;
		/*if ( tick_size == 256 )
		{
			min_increment = 1/(double)256;
		} else {
			min_increment = 0.5;
		}*/
		
	    setMin(1/(double)m_instrument_with_ref_data.getTickSize());
	    setStep(1/(double)m_instrument_with_ref_data.getTickSize());
	    
	    setValue( value / (double)m_instrument_with_ref_data.getTickSize() );
	    
	    setHint( ConvertUtils.getDisplayPrice(m_instrument_with_ref_data, value));
	    
		this.updateState();
		
		final TextItem text_time = this;
		
		this.addChangedHandler( new ChangedHandler()
				{

					@Override
					public void onChanged(ChangedEvent event) {
						// TODO Auto-generated method stub
						
					
							Double current_value = ((Double)getValue() * m_instrument_with_ref_data.getTickSize() );
							
							text_time.setHint( ConvertUtils.getDisplayPrice(m_instrument_with_ref_data, current_value.intValue() ) );
							text_time.updateState();
					}
			
				});
	    
	}
	
	public Integer getValueInTicks()
	{
		Double current_value = ((Double)getValue() * m_instrument_with_ref_data.getTickSize());
		return (int)current_value.doubleValue();
	}
	

}
