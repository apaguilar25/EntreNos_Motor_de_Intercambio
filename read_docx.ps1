param(
    [string]$DocxPath
)

$tempDir = Join-Path $env:TEMP (New-Guid).ToString()
Rename-Item -Path $DocxPath -NewName ($DocxPath + ".zip") -Force
$zipPath = $DocxPath + ".zip"
Expand-Archive -Path $zipPath -DestinationPath $tempDir -Force
Rename-Item -Path $zipPath -NewName (Split-Path $DocxPath -Leaf) -Force

$xmlPath = Join-Path $tempDir "word\document.xml"
if (Test-Path $xmlPath) {
    [xml]$xml = Get-Content $xmlPath -Raw
    $ns = New-Object System.Xml.XmlNamespaceManager($xml.NameTable)
    $ns.AddNamespace("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    
    $paragraphs = $xml.SelectNodes("//w:p", $ns)
    foreach ($p in $paragraphs) {
        $texts = $p.SelectNodes(".//w:t", $ns)
        $paraText = ""
        foreach ($t in $texts) {
            $paraText += $t.InnerText
        }
        if ($paraText.Length -gt 0) {
            Write-Output $paraText
        }
    }
} else {
    Write-Output "word\document.xml not found"
}
Remove-Item -Path $tempDir -Recurse -Force
