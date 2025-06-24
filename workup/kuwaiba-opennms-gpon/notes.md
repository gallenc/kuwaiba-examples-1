integer division in calc macro

```
REM  *****  BASIC  *****

Sub Main

Dim Doc As Object
Dim Sheet As Object
Dim Cell As Object

Doc = ThisComponent
Sheet = Doc.Sheets.getByName("bitterneparkgpon")

Dim splitter As Integer
Dim splitterport As Integer
Dim cabinate As Integer
Dim ont As Integer

Const primary_split As Integer = 8  ' not used
Const secondary_split As Integer = 15  ' split 16 but one spare
Const splitters_par_cab As Integer = 8 ' room for 10 in cabinate 

' interger division \
' 10 splitters per cabinate 30 ports per splitter
For ont = 0 To 2966

   splitterport= ont mod secondary_split ' 0 to 29

   cabinate = ont \ (splitters_par_cab * secondary_split) ' max 300 tails per cabinate
   
   splitter = (ont  mod  (splitters_par_cab * secondary_split) ) \ secondary_split ' max 10 splitters per cabinate

   'info cells print info
   Cell = Sheet.getCellByPosition(28, ont+1)

   Cell.String  = "cab_"+cabinate+"_sp_"+splitter+"_pt_"+splitterport
   
   Cell = Sheet.getCellByPosition(29, ont+1)
   
   Cell.String = ont
   
   ' 
   Cell = Sheet.getCellByPosition(3, ont+1)

   Cell.String  = "cab_"+cabinate+"_sp_"+splitter

   
   
Next ont



End Sub

```
