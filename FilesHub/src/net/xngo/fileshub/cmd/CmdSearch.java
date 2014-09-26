package net.xngo.fileshub.cmd;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;


@Parameters(commandDescription = "Search by different attributes.")
public class CmdSearch
{
  public static String name = "search";
  
  @Parameter(names = {"-uid", "--uid"}, description = "Search by unique ID.", arity = 1)
  public int uid;
  
  @Parameter(names = {"-h", "--hash"}, description = "Search by hash.", arity = 1)
  public String hash;
  
  @Parameter(names = {"-f", "--filename"}, description = "Search by filename. Use * as wildcard.", arity = 1)
  public String filename;
  
  @Parameter(names = {"-p", "--path"}, description = "Search by file path. Use * as wildcard.", arity = 1)
  public String filepath;     
}
