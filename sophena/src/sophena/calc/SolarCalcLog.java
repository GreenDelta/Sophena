package sophena.calc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import sophena.model.Producer;

public class SolarCalcLog {
	private final int fieldWidth = 18;

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
			appendPadded(columnName, fieldWidth);
		sb.append("\r\n");
	}
	
	public void hourValues(int hour, boolean withNewRow, Object... values)
	{
		var sb = getOrCreateSB(currentProducer);
		
		int fractionalDigits = 3;
		
		var sb3 = new StringBuilder("0.");
		for(var k = 0; k < fractionalDigits; k++)
			sb3.append('0');
		
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
		if(withNewRow)
			sb.append("\r\n");
	}

	private void appendPadded(String value, int width)
	{
		var sb = getOrCreateSB(currentProducer);

		var sb2 = new StringBuilder();
		var i = sb2.length();
		sb2.append(value);
		var j = sb2.length();
		var padWidth = Math.max(0, width - (j - i));
		for(var k = 0; k < padWidth; k++)
			sb2.insert(0, ' ');
		sb.append(sb2);
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
			result.append(key != null ? key.name : "");
			result.append("\r\n");
			result.append(sb);
		}
		return result.toString();
	}
	
	public static void writeCsv(String filename, double[] values)
	{
		try {
			PrintWriter printWriter = new PrintWriter(filename);

			printWriter.println("Hour;Value");
			for(var i = 0; i < values.length; i++)
			{
				printWriter.println(String.format("%d;%f", i, values[i]));
			}
			printWriter.close();
		} catch (FileNotFoundException e) {
		}
	}
}
