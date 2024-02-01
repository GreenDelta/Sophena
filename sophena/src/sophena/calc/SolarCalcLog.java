package sophena.calc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import sophena.model.Producer;
import sophena.utils.Strings;

public class SolarCalcLog {
	private Map<Producer, StringBuilder> sbPerProducer = new HashMap<Producer, StringBuilder>();
	private Producer currentProducer;

	public void beginProducer(Producer producer)
	{
		currentProducer = producer;
	}
	
	public void beginDay(int jt, int hour, String... columnNames)
	{
		var sb = getOrCreateSB(currentProducer);

		sb.append("\r\n");
		sb.append("## Day ");
		sb.append(jt);
		sb.append(" (hour ");
		sb.append(hour);
		sb.append(")\r\n");
		sb.append("\r\n");
		for(var columnName: columnNames)
			appendPadded(columnName, 20);
		sb.append("\r\n");
	}
	
	public void hourValues(int hour, Object... values)
	{
		var sb = getOrCreateSB(currentProducer);
		
		int fieldWidth = 20;

		int intDigits = 3;
		int fractionalDigits = 3;
		
		var sb3 = new StringBuilder("0.");
		for(var k = 0; k < fractionalDigits; k++)
			sb3.append('0');
		var decimalFormat = new DecimalFormat(sb3.toString());
		//decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.);
		
		for(var value: values)
		{
			if(value instanceof Double)
			{
				var s = String.format("%1.3f", (Double)value);
				var padWidth = fieldWidth - s.length();
				for(var i=0;i<padWidth;i++)
					sb.append(' ');
				sb.append(s);
			}
			else
				appendPadded(value.toString(), fieldWidth);
		}
		sb.append("\r\n");
	}

	private void appendPadded(String value, int width)
	{
		var sb = getOrCreateSB(currentProducer);

		var i = sb.length();
		sb.append(value);
		var j = sb.length();
		var padWidth = Math.max(0, width - (j - i));
		for(var k = 0; k < padWidth; k++)
			sb.append(' ');
	}
	
	public void message(String s)
	{
		var sb = getOrCreateSB(currentProducer);

		sb.append(s);
		sb.append("\r\n");
	}

	private StringBuilder getOrCreateSB(Producer producer)
	{
		var sb = sbPerProducer.get(producer);
		if(sb == null)
		{
			sb = new StringBuilder();
			sbPerProducer.put(producer, sb);
		}
		
		return sb;
	}

	@Override
	public String toString()
	{
		var result = new StringBuilder();

		var keys = sbPerProducer.keySet();
		for(var key: keys)
		{
			var sb = sbPerProducer.get(key);
			
			result.append("# Producer ");
			result.append(key);
			result.append("\r\n");
			result.append(sb);
		}
		return result.toString();
	}
}
