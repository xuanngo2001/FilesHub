package net.xngo.fileshub.cmd;

import net.xngo.fileshub.Hub;
import net.xngo.fileshub.cmd.Options;
import net.xngo.fileshub.cmd.CmdHash;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Cmd
{

  public Cmd(String[] args)
  {

    Options options = new Options();
    JCommander jc = new JCommander(options);
    jc.setProgramName("FilesHub");
    
    CmdHash cmdHash = new CmdHash();
    jc.addCommand(CmdHash.name, cmdHash);
    
    Hub hub = new Hub();
    try
    {
      jc.parse(args);
      
      if(options.addPaths!=null)
      {
        hub.addFiles(options.getAllUniqueFiles());
      }
      else if(options.update)
      {
        hub.update();
      }
      else if(options.duplicateFiles != null)
      {
        hub.markDuplicate(options.duplicateFiles.get(0), options.duplicateFiles.get(1));
      }
      else
      { // Check if there is a command passed.
        
        if(jc.getParsedCommand().compareTo(CmdHash.name)==0)
        {
          if(cmdHash.addPaths!=null)
          {
            hub.hash(cmdHash.getAllUniqueFiles());
          }
          else
          {
            System.out.println("\nERROR: Wrong usage!\n");            
            jc.usage();
          }
        }
        else
        {
          // Anything else, display the help.
          System.out.println("\nERROR: Wrong usage!\n");
          jc.usage();
        }
      }
     
    }
    catch(ParameterException e)
    {
      System.out.println();
      System.out.println("ERROR:");
      System.out.println("======");
      System.out.println(e.getMessage());
      System.out.println("====================================");
      System.out.println();
      jc.usage();
    }


    
    
  }
  
  
}
