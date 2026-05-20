import zipfile
import xml.etree.ElementTree as ET
import sys

def extract_text_from_docx(docx_path):
    try:
        with zipfile.ZipFile(docx_path) as docx:
            xml_content = docx.read('word/document.xml')
            tree = ET.XML(xml_content)
            
            namespaces = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
            
            text = []
            for paragraph in tree.iterfind('.//w:p', namespaces):
                para_text = []
                for run in paragraph.iterfind('.//w:r', namespaces):
                    for text_node in run.iterfind('.//w:t', namespaces):
                        if text_node.text:
                            para_text.append(text_node.text)
                if para_text:
                    text.append("".join(para_text))
            
            return "\n".join(text)
    except Exception as e:
        return str(e)

if __name__ == '__main__':
    for path in sys.argv[1:]:
        print(f"--- {path} ---")
        print(extract_text_from_docx(path))
        print("\n")
