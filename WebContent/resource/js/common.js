jQuery(function(){
	$(document).delegate('input.check_ids','change',function(){
		if ($(this).is(':checked')){
			$('input.check_id').prop('checked',true);
		}else{
			$('input.check_id').prop('checked',false);
		}
	});
	$(document).delegate('input.check_id','change',function(){
		if ($(this).prop('checked')){
			if ($('input.check_id').length == $('input.check_id:checked').length){
				$('input.check_ids').prop('checked', true);
			}
		}else{
			$('input.check_ids').prop('checked', false);
		}
	});
	$(document).delegate('[submiturl]','click',function(){
		var url = $(this).attr('submiturl');
		if ($(this).attr('resource')!=null && $(this).attr('resource')!=""){
			var target = $($(this).attr('resource'));
			var isAjax = $(this).hasClass("ajax");
			var data = target.serialize();
			if (target.length == 1){
				url += "/"+data.replace(/=/g,"/");
			}else if(target.length > 1){
//				alert("请选择一条数据");//del可以选择多条数据
//				return;
				isAjax = true;
			}
			if(isAjax){
				$.post(url, data, function(res){
					var jRes = JSON.parse(res);
					console.log(jRes);
					if(jRes.errcode == "0"){
						window.location.reload();
					}else{
						alert(2);
					}
				});
			}else{
				window.location.href=url;
			}
		}else{
			window.location.href=url;
		}
	});
});