package dos.common.fs.file;

public class FileTransferParams {
	
	static String DOWNLOAD_REQUEST="__PROVIDE_BLOCK_SENDERS__";
	static String SEND_BLOCK="__SEND_BLOCK__";
	static String ACCEPTANCE_TO_SEND_BLOCK="__ACCEPTANCE_TO_SEND_BLOCK__";
	static String REFUSAL_TO_SEND_BLOCK="__REFUSAL_TO_SEND_BLOCK__";
	
	static String UPLOAD_REQUEST="__PROVIDE_BLOCK_ACCEPTORS__";
	static String HOST_BLOCK="__HOST_BLOCK__";
	static String ACCEPTANCE_TO_HOST_BLOCK="__ACCEPTANCE_TO_HOST_BLOCK__";
	static String REFUSAL_TO_HOST_BLOCK="__REFUSAL_TO_HOST_BLOCK__";
	
	static String requestFileNameDelimiter=" ";
	static String fileNameBlockIdDelimiter="___";
	static String blockIdblockSizeDelimiter=")";
	static String fileLoaderCustomFooterDelim="~";
	static char replacementForForwardSlash='^';
	
}
