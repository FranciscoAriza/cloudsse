# Searchable symmetric encryption (SSE) Lab

SSE allows you to store information at an untrusted server, so you can make further inquiries about this information, guaranteeing your privacy throughout the process. In this Lab, you will work with two implementations of this technique, in order to understand the foundations on which it is built as well as the data structures and security primitives that are needed for its development. To do so, we will use a Library called Clusion that implements different variations of SSE.

## The Clusion Library

Clusion is an easy to use software library for searchable symmetric encryption
(SSE). Its goal is to provide modular implementations of various
state-of-the-art SSE schemes. Clusion includes constructions that handle
single, disjunctive, conjunctive and (arbitrary) boolean keyword search.  All
the implemented schemes have *sub-linear* asymptotic search complexity in the
worst-case.  

Clusion is provided as-is under the *GNU General Public License v3 (GPLv3)*. 


## Implementation

*Indexing.* The indexer takes as input a folder that can contain pdf files,
Micorosft files such .doc, .ppt, media files such as pictures and videos as
well as raw text files such .html and .txt. The indexing step outputs two
lookup tables. The first associates keywords to document filenames while the
second associates filenames to keywords. For the indexing, we use Lucene to
tokenize the keywords and get rid of noisy words.  For this phase, Apache
Lucene, PDFBox and POI are required. For our data structures, we use Google
Guava.

*Cryptographic primitives.* All the implementations make use of the Bouncy
Castle library. The code is modular and all cryptographic primitives are
gathered in the `CryptoPrimitives.java` file.  The file contains AES-CTR,
HMAC_SHA256/512, AES-CMAC, key generation based on PBE PKCS1 and random string
generation based on SecureRandom.  It also contains a synthetic IV AES encryption and AES based authenticated encryption. 
In addition, it also contains an
implementation of the HCB1 online cipher from \[[BBKN07][BBKN07]\]. 


## Schemes to test 

In this Lab, you will test the following SSE schemes:

+ **2Lev**:  a static and I/O-efficient SSE scheme \[[CJJJKRS14][CJJJKRS14]]\. 

+ **Dyn2Lev**:  a dynamic variation of \[[CJJJKRS14][CJJJKRS14]], comes with two instantiations, a first instantiation that 
only handles add operations, and a second one that handles delete operations in addition. Both instantiations have forward-security guarantees but at the cost of more interactions and non-optimality (in the case of delete). 

## Build Instructions (over the Virtual Machine of the course)

+ Download/Git clone this repository
+ Run below commands to build the jar

	`cd SSELab`
	
	`mvn clean compile assembly:single`
	
	`cd target`
	
	`ls SSELab-1.0-SNAPSHOT-jar-with-dependencies.jar`
	
+ If the above file exists, build was successful and contains all dependencies


## Procedure

In order to test the previously introduced schemes, follow the next steps:

+ Create two new directories and store some input files. You can include .pdf .docx .pptx .html or .txt files. You are going to perform queries over this information, so it is recommended that the files are composed mostly of text.

###### Important: The names of the files should not have whitespaces. 

+ Create other directory that will contain the key and index files of both implementations.

+ Run the previously generated .jar by executing the command below

	`java -jar SSELab-1.0-SNAPSHOT-jar-with-dependencies.jar`

+ Start testing the first option of the main menu (the static implementation). This option has two possible commands 1. Test indexing and query, 2.Test files encryption and query over those files. The first command creates a secure index, but the information is kept in plain text. The second commands allows you to encrypt the files.

	Notice that in this case the operation is static; first, you give a set of documents; second, the associated index is created; third, you can search based on keywords of your choice. However, you cannot make any updates over your 		index. Study the associated implementation and the library used in the generateKey(), buildIndex() and query() methods.

###### Recommendations: 
1. After the successful generation of the index, verify that it has been correctly stored in the folder you selected along with the secret key. Then, open the file containing the index and notice that it is fully encrypted (so it does not reveal any information about the contents of the files).
2. If you chose the second option (Test files encryption and query over those files), after index building, verify that the files were properly encrypted. To do this, try to open them from your preferred editor and notice that it is not possible to see their content. 

	Then, when you perform some queries, you will have the option to decrypt the returned files. Choose this option and 		verify that your files were properly decrypted (seeing that their content is accurate and complete).

+ Now, try the second the second option of the main menu (the dynamic implementation). You will notice that, with the first command of this option, it is possible to create and work with an index and update or delete previously indexed documents (which is why it's a dynamic implementation). In order to understand the associated changes and the impact of these updates, be sure to understand the updateIndex() and deleteElement() methods.

> Notice that, the second command (Test files encryption and query over those files) is incomplete, this is the extension that you must complete. 

## References

1. \[[CJJJKRS14](https://eprint.iacr.org/2014/853.pdf)\]:  *Dynamic Searchable Encryption in Very-Large Databases: Data Structures and Implementation* by D. Cash, J. Jaeger, S. Jarecki, C. Jutla, H. Krawczyk, M. Rosu, M. Steiner.


[CJJJKRS14]: https://eprint.iacr.org/2014/853.pdf

