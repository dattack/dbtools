<#if missingSource??>
    <b>[${status}]</b> Unsatisfied join condition (${joinCondition}): missing data on '${missingSource}'
	<#if rowDataList??>
	    <#list rowDataList as rowData>
	      <#if rowData.sourceId.value != missingSource>
		      <table cellspacing="0" cellpadding="3" border="1" bordercolor="#013ADF">
		        <tr bgcolor="#013ADF" style="font-weight: bold;color: #FFFFFF;">
		            <td>Source</td>
		        <#list rowData.fieldValueList as item>
		            <td>${item.key.value}</td>
		        <#else>
		            <td>&nbsp;</td>
		        </#list>
		        </tr>
		        <tr>
		            <td>${rowData.sourceId.value}</td>
		        <#list rowData.fieldValueList as item>
		            <td>${item.value}</td>
		        <#else>
		            <td><i>Empty ResultSet</i></td>
		        </#list>
		        </tr>
		      </table>
	      </#if>
	    </#list>
	</#if>
</#if>

<#if checkExpr??>
    <b>[${status}]</b> Unsatisfied check expression: ${checkExpr}
	<#if rowDataList??>
	    <#list rowDataList as rowData>
	      <table cellspacing="0" cellpadding="3" border="1" bordercolor="#013ADF">
	        <tr bgcolor="#013ADF" style="font-weight: bold;color: #FFFFFF;">
	            <td>Source</td>
	        <#list rowData.fieldValueList as item>
	            <td>${item.key.value}</td>
	        <#else>
	            <td>&nbsp;</td>
	        </#list>
	        </tr>
	        <tr>
	            <td>${rowData.sourceId.value}</td>
	        <#list rowData.fieldValueList as item>
	            <td>${item.value}</td>
	        <#else>
	            <td><i>Empty ResultSet</i></td>
	        </#list>
	        </tr>
	      </table>
	    </#list>
	</#if>
</#if>
