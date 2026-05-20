# /// script
# requires-python = ">=3.11"
# dependencies = [
#     "python-docx",
# ]
# ///
import sys
import docx
import os

for file in sys.argv[1:]:
    try:
        doc = docx.Document(file)
        basename = os.path.basename(file)
        out_name = basename.replace(".docx", ".txt")
        with open(out_name, "w", encoding="utf-8") as f:
            for p in doc.paragraphs:
                f.write(p.text + "\n")
        print(f"Successfully processed {file} into {out_name}")
    except Exception as e:
        print(f"Error processing {file}: {e}")
