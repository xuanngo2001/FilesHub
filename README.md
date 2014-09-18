FilesHub
========

FilesHub is used to remove duplicate files.

Usage
======

Add files or directories
```
# FilesHub -a <files, directories or mix>
FilesHub -a FilesHub.db ./ FilesHub*
```

Mark a file is a duplicate of another.
```
# FilesHub -d <This file.txt> <Is a duplicate of this file.txt>
FilesHub -d SomeRandomFile.txt FilesHub.db
```

Display the hash of files
```
# FilesHub hash <files, directories or mix>
FilesHub hash FilesHub.db ./ FilesHub*
```

