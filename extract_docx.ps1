Add-Type -AssemblyName System.IO.Compression.FileSystem

function Extract-DocxText {
    param([string]$DocxPath, [string]$OutputPath)
    
    try {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($DocxPath)
        $entry = $zip.Entries | Where-Object { $_.FullName -eq 'word/document.xml' }
        if ($null -eq $entry) {
            "No word/document.xml found" | Out-File $OutputPath -Encoding UTF8
            $zip.Dispose()
            return
        }
        $stream = $entry.Open()
        $reader = New-Object System.IO.StreamReader($stream)
        $xmlStr = $reader.ReadToEnd()
        $reader.Close()
        $stream.Close()
        $zip.Dispose()
        
        [xml]$xml = $xmlStr
        $ns = New-Object System.Xml.XmlNamespaceManager($xml.NameTable)
        $ns.AddNamespace("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        
        $paragraphs = $xml.SelectNodes("//w:p", $ns)
        $result = ""
        foreach ($p in $paragraphs) {
            $texts = $p.SelectNodes(".//w:t", $ns)
            $paraText = ""
            foreach ($t in $texts) {
                $paraText += $t.InnerText
            }
            if ($paraText.Length -gt 0) {
                $result += $paraText + "`r`n"
            }
        }
        $result | Out-File -FilePath $OutputPath -Encoding UTF8
    } catch {
        $_.Exception.Message | Out-File -FilePath $OutputPath -Encoding UTF8
    }
}

Extract-DocxText -DocxPath "C:\Users\Personal\.gemini\antigravity\scratch\entreNos\Documentacion\BRIEF EQUIPO 03.docx" -OutputPath "C:\Users\Personal\.gemini\antigravity\scratch\entreNos\brief.txt"
Extract-DocxText -DocxPath "C:\Users\Personal\.gemini\antigravity\scratch\entreNos\Documentacion\ERS EQUIPO 03 - Ing Software.docx" -OutputPath "C:\Users\Personal\.gemini\antigravity\scratch\entreNos\ers.txt"
