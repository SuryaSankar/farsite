package dos.common.util;
import java.io.*;
import java.util.*;

public class Run {
	public Run(String[] args){
		try {
			Runtime runTime=Runtime.getRuntime();
			Process process=runTime.exec(args);
			BufferedReader error=new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String errorLine;
			while((errorLine=error.readLine())!=null)
				System.out.println("Error! "+errorLine);

			BufferedReader output=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String outputLine;
			while((outputLine=output.readLine())!=null)
				System.out.println(outputLine);
			int exitvalue=process.waitFor();
			System.out.println("Exit Value:"+exitvalue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	public static void main(String[] args) {
		new Run(args);
	}

}
