<pre>
<#attempt>
  execution ID: ${executionId}
<#recover>
  execution ID: Ops! This value is not available.
</#attempt>
<#attempt>
  successRows: ${successRows}
  warningRows: ${warningRows}
  errorRows: ${errorRows}
<#recover>
  Ops! Statistics are not available. Check log file for details.
</#attempt>
</pre>

${log}