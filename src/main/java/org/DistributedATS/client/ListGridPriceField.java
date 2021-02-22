package org.DistributedATS.client;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class ListGridPriceField extends ListGridField {
	
	public ListGridPriceField( String name, String title )
	{
		super(name, title);
		setCellFormatter(new CellFormatter() {
		      public String format(Object value, ListGridRecord record, int rowNum,
		                           int colNum) {
		        if (value == null)
		          return null;
		        try {
		          NumberFormat nf = NumberFormat.getFormat("#,##0.00");
		          return nf.format(((Number)value).floatValue());
		        } catch (Exception e) {
		          return value.toString();
		        }
		      }
		    });
	}

}
