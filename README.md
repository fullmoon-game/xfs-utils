# xfs-utils

Very basic Kotlin library for working with XFS (Xenesis File System) archives.

XFS is a very old & obscure file format used in some old video games such as WolfTeam.

In its current state, this library is only able to read XFS files and extract their contents. Additionally only version 1.0.0.0 of the format is supported.

Writing/repacking is not yet supported.

# Usage

Load up this project in IntelliJ, and run the "Unpack File" run configuration.

The application will then prompt you to select an XFS input file and an output directory where the extracted files will be saved.

If everything goes well, the files will be extracted to the output directory.

# Credits

Most of the code is based on a QuickBMS script made by Luigi Auriemma, which can be found here: https://aluigi.altervista.org/quickbms.html
