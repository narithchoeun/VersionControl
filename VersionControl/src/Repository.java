import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;
import java.io.*;
import java.nio.file.Files;

/**
 * ask the user to input a source they would like to create a repository for
 * select a destination to store that repository
 * in the repository create a manifest folder and folders for each file in the source
 * the manifest contains the 'commits' meaning that it contains the different files and the changes to the repository
 * the file folders will contain different versions(artifacts) of the file if any changes were 'committed' 
 */

public class Repository 
{
	public static void main(String[] args)
	{
		Scanner scan = new Scanner(System.in);
		Repository repo = new Repository(get_source());	
		repo.create_repo();
//		repo.find_mg("05-07-2016 9.23.01 PM.txt", "05-07-2016 9.17.24 PM.txt"); // (src,tgt)
//		File src = new File("/Users/narithchoeun/Desktop/src");
//		repo.merge_conflict(src, "h.txt", "-79.txt", "72.txt", "72.txt");
		
//		repo.merge("05-08-2016 2.34.43 PM.txt", "/Users/narithchoeun/Desktop/src");
		
		/* 
		 * displays message to user which will continue to wait for the user to check in/out or end the program
		 */
		int option; 
		do{
			System.out.println("Waiting for user to check in, check out, merge or quit.\n" +
					"1. Check in\n" + 
					"2. Check out\n" +
					"3. Merge\n" + 
					"4. Exit\n");
			System.out.print("Select menu option: "); 
			option = scan.nextInt();
			switch(option){
			case 1:
				repo.chkin();
				break;
			case 2: 
				in = new Scanner(System.in);
				System.out.println("What version of the project would you like to check out?(MM-dd-yyyy h.mm.ss a.txt)");
				String ver;
				ver = in.nextLine();
				
				System.out.println("Where do you want to store this checkout project?");
				String dest;
				dest = in.nextLine();
			
				repo.chkout(ver, dest);
				break;
			case 3: 
				in = new Scanner(System.in);
				//user selects current man file for the project tree to be merged
				System.out.println("What manifest file do you want to merge?");
				String manfile;
				manfile = in.nextLine();
				
				//user selects project tree to merge to 
				System.out.println("What project tree do you want to merge these files to?");
				String ptree;
				ptree = in.nextLine();
				
				repo.merge(manfile, ptree);
				break;
			case 4: 
				System.out.println("Done.");
				break;
			}
		}while (option != 4);
		scan.close();
	} // end of main
	
	
	
	//class variables
	static Scanner in = new Scanner(System.in);
	private PrintWriter out; 
	private File src_file = null, tgt_file = null, repo = null; 
	private String recent_chkin = "", mergeman = "", oldmani = "";
	private File tgtmani = null, tgtpath = null, srcpath = null;
	private ArrayList<String> src_manifests = new ArrayList<>();
	private ArrayList<String> tgt_manifests = new ArrayList<>();
	
	/**
	 * Initializes the source file for the repository 
	 * @param s source file path for repository 
	 */
	public Repository(String s)
	{
		src_file = new File(s) ; 
	} // end of Repository constructor 
	
	/**
	 * Creates a repository for a new or existing file
	 */
	public void create_repo()
	{
		tgt_file = get_target();
		repo = tgt_file; //sets repo to tgt for later use
		
		boolean created = tgt_file.mkdir();
		if(created) 
			System.out.println("Repository created.");
		else if(tgt_file.isDirectory())
			System.out.println("Folder already exists");
		else 
			System.out.println("Repository was not created.");

		create_manifest(src_file, tgt_file) ; 
		copy_source(src_file, tgt_file) ; 
	}
	
	/**
	 * Gets the file path from the source file 
	 * @return source file path 
	 */
	public static String get_source()
	{
		System.out.println("Select the pathname for a source folder");
//		String source = in.nextLine();
		String source = "/Users/narithchoeun/Desktop/src";
		return source;
	}
	
	/**
	 * Getting the target folder specified by the user 
	 * @return target file 
	 */
	public File get_target()
	{
		System.out.println("Select the pathname a target folder");
//		String pathname = in.nextLine();
		String pathname = "/Users/narithchoeun/Desktop";
		pathname += "/repo343";
		
		File target_dir = new File(pathname);
		return target_dir;
	} // end of get_target method
	
	/**
	 * Copying the source file(s) into a specified target 
	 * @param source File to be copied 
	 * @param target File copied from source 
	 */
	public void copy_source(File source, File target)
	{
		//creates project tree folder
		File ptree_dir = new File(target+"/"+source.getName());
		ptree_dir.mkdir();
		
		
		/* iterates through the files in the source folder and copies files into target folder */
		copyDirectoryContents(source, ptree_dir); 
	} // end of copy_source method 
	
	/**
	 * 
	 * @param f
	 * @param tgt
	 */
	public void copyDirectoryContents(File f, File tgt)
	{
		for(File select_file : f.listFiles())
		{
			if(select_file.isDirectory()){
				File temp = new File(tgt.getPath() + "/" + select_file.getName()); 
				temp.mkdir(); 
				copyDirectoryContents(select_file, temp);
			}
			else
				try {
					if(select_file.isHidden() || select_file.getName().startsWith("currentMom"));
					else {
					in = new Scanner(select_file); //read the file
					
					//file path to create directories that contains the source file's artifacts
					File temp_dir = new File(tgt.getPath()+"/"+select_file.getName()) ; 
					temp_dir.mkdir();
					
					//write into the created directory with an artifact of the file
					File write_file = new File(temp_dir.getPath()+"/"+checksum(select_file)+get_extension(select_file)) ;
					out = new PrintWriter(write_file);
					
					
					//reads src file and copies content into artifact file
					while(in.hasNextLine()){
						out.write(in.nextLine());
					} // end of while loop 
					
					out.flush();
					in.close();
					}
				} catch (IOException e) { e.printStackTrace(); } // end of try catch block
		}
	} 
	
	/**
	 * Creates the manifest folder for a directory and generates a man file
	 * @param src File that was made into a repository/clone
	 * @param tgt File that will store the manifest dir
	 * @param recent String that stores the most recent check in
	 */
	public void create_manifest(File src, File tgt)
	{
		String path = tgt.getPath() + "/manifest"; 

		File manifest = new File(path) ; 
		manifest.mkdir() ; 
		String time = get_timestamp();
		
		File readmom = null;
		Scanner scanmom;

		//creates man line file with check in timestamp and the project hierarchy
		File man_line = new File(manifest.getPath()+"/"+time+".txt");
//		oldmani = man_line.getName();
		tgtmani = man_line; //store tgt for merging 
		
		File currentMom = new File(src.getPath() + "/currentMom.txt");
		try {
			currentMom.createNewFile();
		} catch (IOException e) { e.printStackTrace(); }
		//*NOTE no currentMom should exist when creating repo*
			
			//find currentMom
			for(File sel : src.listFiles()){
				if (sel.getName().startsWith("currentMom.txt")){
					readmom = sel;
					break; //break out of for loop
				}
			}
		
			//read currentMom and store recent check in from currentMom
			try {
				scanmom = new Scanner(readmom);
				if(scanmom.hasNextLine()){
					recent_chkin = scanmom.nextLine();
				}
			scanmom.close();
			} catch (IOException e) { e.printStackTrace(); }
		
		
		try{
			out = new PrintWriter(man_line);
			out.println(time);
			out.println("Mom: " + recent_chkin);
			out.println("@" + src.getParent()); 
			iterateThroughDirectory(src, ("/" + src.getName())); 
		} catch (IOException e) { e.printStackTrace(); }
		
		//write to a current mom file with current timestamp
		try {
			out = new PrintWriter(currentMom);
			out.write(man_line.getName());
			out.flush();
		}catch (IOException e) { e.printStackTrace(); }
		
		recent_chkin = man_line.getName(); //update class variable 
	} // end of create_manifest method
	
	/**
	 * Allows to iterate through project folder and 
	 * print file paths to manifest
	 * @param f File to iterate through 
	 * @param s File name
	 */
	public void iterateThroughDirectory(File f,String s)
	{
		for(File select_file : f.listFiles())
		{
			if(select_file.isDirectory())
			{
				s += "/" + select_file.getName() ; 
				iterateThroughDirectory(select_file, s);
			} // end of if 
			else
				if(select_file.isHidden() || select_file.getName().startsWith("currentMom"));
				else {
					File cpy = new File(s + "/" + select_file.getName() +" "+checksum(select_file)+get_extension(select_file)) ; 
					out.write(cpy.getPath() + "\n"); 
				} // end of else 
		} // end of for each loop 
		out.flush();
	} // end of iterateThroughDirectory method 
	
	/**
	 * Get the current date and time
	 * @return A string of the current date and time
	 */
	public String get_timestamp()
	{
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy h.mm.ss a");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	/**
	 * Calculates the checksum of a file 
	 * @param f File to be scanned
	 * @return checksum byte of a file 
	 */
	public byte checksum(File f)
	{
		byte checksum = 0; 
		
		try {
			for(byte b : Files.readAllBytes(f.toPath()))
				checksum += b ; 
		} catch (IOException e) {
			e.printStackTrace();
		}  
		
		return checksum ; 
	} // end of checksum method 
	
	
	/**
	 * Gets the extension for a file by parsing the filename at the last period
	 * @return A file extension string
	 */
	public String get_extension(File f)
	{
		String filename = f.getName();
		int i = filename.lastIndexOf(".");
		String ext = filename.substring(i);
		return ext;
	}
	
	
	/**
	 * Checks in the repo, updating the manifest
	 */
	public void chkin()
	{
		in = new Scanner(System.in);
		System.out.println("What is the source path?");
		String src = in.nextLine();
		File srcpath = new File(src);
		
		System.out.println("Checking in...\n");
		/* although in copy_source we use mkdir() calls, when checking in it won't create
 		 * a new directory it will know the folder/file already exists and won't update
		 * the repository. Any existing files with a different checksum will be added to the repository.
		 * the man-file will only write the most updated file
		 */
		copy_source(srcpath, repo);
		create_manifest(srcpath, repo);
	}
	
	/** Overloaded **
	 * Checks in the repo, updating the manifest
	 */
	public void chkin(File src)
	{
		System.out.println("Checking in...\n");
		copy_source(src, repo);
		create_manifest(src, repo);
	}
	
	
	/**
	 * check out a version of the repo
	 */
	public void chkout(String ver, String dest)
	{
		File dest_dir = new File(dest + "/chkout");
		dest_dir.mkdir();
		
		File man_dir = new File(repo.getPath()+"/manifest");

		//creates project tree folder
		File ptree_dir = new File(dest_dir.getPath()+"/"+src_file.getName());
		ptree_dir.mkdir();
		
		Scanner scan;
		
		//look through manifest dir and find matching requested version
		for(File sel_file : man_dir.listFiles()){
			if(sel_file.isHidden());//do nothing for hidden files
			else {
				//if date matches user input, read the file that matches input
				if(sel_file.getName().startsWith(ver)){
					try{
						in = new Scanner(sel_file); //assign scanner to read that file
						recent_chkin = sel_file.getName();
					} catch(FileNotFoundException e){ e.printStackTrace(); }
					break; //break out if file is found
				}
			}
		}
		
		//read man file and grab paths to be copied
		while(in.hasNextLine()){
			String path = in.nextLine();
			if(path.startsWith("/") || path.startsWith("\\")){
				String[] filevar = path.split(" "); //splits line by whitespace
				File filegrab = new File(filevar[0]);
				String chksum = filevar[1];
				
				File sel = new File("/"+repo.getPath()+"/"+filegrab.getPath()+"/"+chksum);
				try{
					scan = new Scanner(sel);
					String[] outtree = filegrab.getPath().split("/");
					String line = "";
					for(int i = 0; i < outtree.length-1; i++){
						line +="/"+outtree[i];
						File dir = new File(dest_dir.getPath()+line);
						dir.mkdir();
					}
					File output = new File(ptree_dir.getPath()+"/"+filegrab.getName());

					out = new PrintWriter(output);
					while(scan.hasNextLine()){
						out.write(scan.nextLine());
					}
					scan.close();
					out.flush();
				} catch (IOException e) { e.printStackTrace(); }
			}
		}//end of reading man file
		
		create_manifest(ptree_dir, repo);
	}
	
	/**
	 * 
	 * @param man
	 * @param ptree
	 */
	public void merge(String man, String tgt)
	{
		//store target path
		mergeman = man;
		tgtpath = new File(tgt);
		oldmani = tgtpath.getName(); //for conflict_manifest()
		
		//"check in" target path, programmer checks in for the user
		System.out.println("Checking in target project tree");
		chkin(tgtpath);
		
		String srcname = "";
		
		System.out.println("Merging...\n");
		//path to repo manifest
		File man_dir = new File(repo.getPath() + "/manifest");
		
		//grab selected manifest file
		for(File sel_file : man_dir.listFiles()){
			if(sel_file.isHidden());//do nothing for hidden files
			else {
				//if date matches user input, read the file
				if(sel_file.getName().startsWith(man)){
					try{
						in = new Scanner(sel_file); //assign scanner to read that file
						recent_chkin = sel_file.getName();
					} catch(FileNotFoundException e){ e.printStackTrace(); }
					break; //break out if file is found
				}
			}
		}
		
		File localtgt = tgtmani;
		//read man file 
		while(in.hasNextLine()){
			String path = in.nextLine();

			if(path.startsWith("@")){
				String[] pathsplit = path.split("@");//split path so it doesn't include @
				srcname = pathsplit[1]; //save for when recreating manifest file if a merge conflict occurs
				srcpath = new File(srcname +"/"+ src_file.getName());
//				System.out.println("srcpath: " + srcpath.getPath());
			}
			
			//store file and AID
			if(path.startsWith("/") || path.startsWith("\\")){
				String[] filevar = path.split(" "); //splits line by whitespace
				File filegrab = new File(filevar[0]);
				String chksum = filevar[1];
				Scanner scan;
				
				//grab file from repo
				File sel = new File("/"+repo.getPath()+"/"+filegrab.getPath()+"/"+chksum);
//				System.out.println("selected " + sel.getPath());
				//compare file with target project
				try {
//					System.out.println("tgtmani " + localtgt.getPath());
					Scanner tgtscan = new Scanner(localtgt);
									
					while (tgtscan.hasNextLine()){
						String tgtline = tgtscan.nextLine();
					
						if(tgtline.startsWith("/") || tgtline.startsWith("\\")){
							String[] filetgt = tgtline.split(" "); //splits line by whitespace
							File tgtgrab = new File(filetgt[0]);
							String tgtchksum = filetgt[1];
							
//							System.out.println("tgtgrab: " + tgtgrab.getName() + " " + tgtchksum);
							//if the src and tgt have the same name but different AID, mergeconflict()
							if(tgtgrab.getName().equals(filegrab.getName()) && !chksum.equals(tgtchksum)){
								System.out.println("merge conflict");
								System.out.println("src " + filegrab.getName() + " " + chksum);
								System.out.println("tgt " + tgtgrab.getName() + " " + tgtchksum);
								
								//create 3 files to target project tree
								String gchksum = find_mg(man, tgtmani.getName(), filegrab.getName());								
								
								merge_conflict(tgtpath, filegrab.getName(), chksum, tgtchksum, gchksum);
							} 
								//write tgtgrab to target project tree because the source project tree either doesn't have it or has the same file
								File tgt_read = new File(repo.getPath() +"/"+ src_file.getName() +"/"+filegrab.getName()+"/"+chksum);
								File tgt_write = new File(tgtpath.getPath()+"/"+filegrab.getName());
//								System.out.println("tgt_write" + tgt_write.getPath());
								try {
									scan = new Scanner(tgt_read);
									out = new PrintWriter(tgt_write);
									while(scan.hasNextLine()){
										out.write(scan.nextLine());
									}
									out.flush();
									scan.close();
								} catch (IOException e) { e.printStackTrace(); }
							
						}
					}
					tgtscan.close();
				} catch (IOException e) { e.printStackTrace(); }
			}
		}//end of reading man file	
		
		//create manifest with merge conflict files
		conflict_manifest(tgtpath, srcpath);
	}//end of merge
	
	/**
	 * 
	 * @param tgt
	 * @param conflictfile
	 * @param src_chksum
	 * @param tgt_chksum
	 * @param mg
	 */
	public void merge_conflict(File tgt, String conflictfile, String src_chksum, String tgt_chksum, String gchksum){
		Scanner scan;
		String[] csplit = conflictfile.split("\\."); // need backslashes for special character like a period
		String conflictname = csplit[0]; //only works if no other periods are in the filename 
		//look in repo src
		File src_dir = new File(repo.getPath() +"/"+src_file.getName());
		
		//find mr file in repo
		String mr = chk_dir(src_dir, conflictfile, src_chksum);
		File mr_file = new File(mr);
		
		//find mt file in repo
		String mt = chk_dir(src_dir, conflictfile, tgt_chksum);
		File mt_file = new File(mt);
		
		//find mg file in repo
		String mg = chk_dir(src_dir, conflictfile, gchksum);
		File mg_file = new File(mg);
		
		//rename mr file to tgt project tree
		File old = new File(tgt.getPath() + "/" + conflictfile);
		File mr_write = new File(tgt.getPath() + "/" + conflictname +"_MR" + get_extension(mr_file));
		old.renameTo(mr_write);
		
		//write mt file to tgt project tree
		try{
			scan = new Scanner(mt_file);
			File mt_write = new File(tgt.getPath() + "/" + conflictname + "_MT" + get_extension(mt_file));
			out = new PrintWriter(mt_write);
			while(scan.hasNextLine()){
				out.write(scan.nextLine());
			}
			out.flush();
			scan.close();
			
			//write mg file to tgt project tree
			scan = new Scanner(mg_file);
			File mg_write = new File(tgt.getPath() + "/" + conflictname + "_MG" + get_extension(mg_file));
			out = new PrintWriter(mg_write);
			while(scan.hasNextLine()){
				out.write(scan.nextLine());
			}
			out.flush();
			scan.close();
		} catch (IOException e) { e.printStackTrace(); }		
	}
	
	/**
	 * checks a directory and searches for a specified file 
	 * @param f
	 * @param chk
	 * @param chksum
	 * @return an empty string if nothing is found
	 */
	public String chk_dir(File f,String chk, String chksum)
	{
		for(File select_file : f.listFiles())
		{
			if(select_file.isDirectory() && !select_file.getName().equals(chk))
			{
				chk_dir(select_file, chk, chksum);
			} // end of if 
			else
				if(select_file.isHidden() || select_file.getName().startsWith("currentMom"));
				else {
					String grab = (f + "/" + select_file.getName() + "/" + chksum);
					return grab;
				} // end of else 
		} // end of for each loop 
		return ""; //return nothing if not found, shouldn't happen when merge conflict calls it
	} // end of chk_dir method 
	
	/**
	 * 
	 * @param src manifest file to be merged
	 * @param tgt manifest file to merge with
	 * @return common ancestor of both files
	 */
	public String find_mg(String src, String tgt, String conflictfile){		
		//traverse back up the tree starting from src manifest
		find_mom(new File(repo.getPath() + "/manifest/"), src, src_manifests);
		
		//traverse back up the tree starting from the tgt manifest
		find_mom(new File(repo.getPath() + "/manifest/"), tgt, tgt_manifests);
		
		//reverse the orders of the arraylists so you can start at the first check in
		Collections.reverse(src_manifests);
		Collections.reverse(tgt_manifests);
		
		//traverse back down the tree until there is a split 
		int i = 0;
		boolean found = false;
		String tmp_src = "", tmp_tgt = "";
		//compare two manifest until the branch is found or the iterator exceeds either of the arraylist
		while(!found && i < src_manifests.size() && i < tgt_manifests.size()){
			tmp_src = src_manifests.get(i);
			tmp_tgt = tgt_manifests.get(i);
			
			//compare the two manifest to see if they match 
			if (!tmp_src.equals(tmp_tgt)){
				found = true;
				break;
			} else {
				i++;
			}
		}
		
		//read grandpa manifest file and find grandpa file
		File gpa_man = new File(repo.getPath() + "/manifest/" + src_manifests.get(--i));
		Scanner scan;
		String gchksum = "";
		try{
			scan = new Scanner(gpa_man);
			while(scan.hasNextLine()){
				String gpaline = scan.nextLine();
				if(gpaline.startsWith("/") || gpaline.startsWith("\\")){
					String[] filegpa = gpaline.split(" "); //splits line by whitespace
					File gpagrab = new File(filegpa[0]);
					
					//if the gpagrab is the same as the conflict file store that checksum as the grandpa file
					if(gpagrab.getName().equals(conflictfile)){
						gchksum = filegpa[1];
					}
				}
			}
			scan.close();
		} catch (IOException e) { e.printStackTrace(); }
		//returns grandpa file chksum
		return gchksum; //returns one element before the branch occurred
	}
	
	/**
	 * 
	 * @param man
	 * @param mom
	 * @param list
	 */
	public void find_mom(File man, String mom, ArrayList<String> list)
	{
		Scanner scan = null;
	
		//traverse back up the tree starting from src manifest
		try{
			File manmom = new File(man.getPath()+"/"+mom);
			scan = new Scanner(manmom);
			while(scan.hasNextLine()){
				String path = scan.nextLine();

				//store mom file
				if(path.startsWith("Mom: ")){
					String[] momsplit = path.split(" ");
					if (momsplit.length > 1){
						mom = momsplit[1] + " " + momsplit[2] + " " + momsplit[3];
						list.add(mom);
						break;//break out of reading manifest
					} else {
						return; //end recursion
					}
				}
			}
		find_mom(man, mom, list);
		} catch (IOException e) { e.printStackTrace(); }
		finally { //clean up scan
			if (scan != null)
				scan.close();
		}
	}
	
	public void conflict_manifest(File src, File tgt)
	{
		File manifest = new File(repo.getPath() + "/manifest"); 
		String time = get_timestamp();
		
		File readmom = null;
		Scanner scanmom;

		//creates man line file with check in timestamp and the project hierarchy
		File man_line = new File(manifest.getPath()+"/"+time+"_log.txt");
		tgtmani = man_line; //store tgt for merging 
		
		File currentMom = new File(src.getPath() + "/currentMom.txt");
		try {
			currentMom.createNewFile(); //won't create if it already exists
		} catch (IOException e) { e.printStackTrace(); }
			
			//find currentMom
			for(File sel : src.listFiles()){
				if (sel.getName().startsWith("currentMom.txt")){
					readmom = sel;
					break; //break out of for loop
				}
			}
		
			//read currentMom and store recent check in from currentMom
			try {
				scanmom = new Scanner(readmom);
				if(scanmom.hasNextLine()){
					recent_chkin = scanmom.nextLine();
				}
			scanmom.close();
			} catch (IOException e) { e.printStackTrace(); }
		
		
		try{
			out = new PrintWriter(man_line);
			out.println(time);
			out.println("Mom: " + recent_chkin);
			out.println("Mom to be merge: " + mergeman);
			out.println("Target mom: " + recent_chkin);
			out.println("@" + src.getParent()); 
			iterateThroughDirectory(src, ("/" + src.getName())); 
		} catch (IOException e) { e.printStackTrace(); }
		
		//write to a current mom file with current timestamp
		try {
			out = new PrintWriter(currentMom);
			out.write(time+".txt");
			out.flush();
		}catch (IOException e) { e.printStackTrace(); }
		
		recent_chkin = man_line.getName(); //update class variable 
	} // end of create_manifest method
} // end of Repository Project