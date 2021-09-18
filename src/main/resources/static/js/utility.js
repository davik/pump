
$.ajaxSetup({
    beforeSend: function(xhr) {
        let token = $("meta[name='_csrf']").attr("content");
        xhr.setRequestHeader('X-CSRF-TOKEN', token);
    }
});

function fetchAccountDetail(id) {
    $('#nav-profile-tab').trigger('click');
    $('#id').val(id);
    $('#idSearch').click();
}

function fetchProductDetail(id) {
    $('#nav-profile-tab').trigger('click');
    $('#id').val(id);
    $('#productIdSearch').click();
}

function getVoucherForm () {
    if ($("#voucherType").val() == "PURCHASE" || $("#voucherType").val() == "SALE") {
        $("#formPayment").addClass("d-none");
        $("#formPurchase").removeClass("d-none");
    } else if ($("#voucherType").val() == "PAYMENT" || $("#voucherType").val() == "RECEIPT") {
        $("#formPayment").removeClass("d-none");
        $("#formPurchase").addClass("d-none");
    }
}

function changeAncAttSess() {
    $('#to').attr({min: $('#from').val()});
    if ($('#from').val() && $('#to').val()) {
        $('#stockReport').removeClass("disabled");
    }
    let url = '/stockReport?' + $.param({
                from: $('#from').val(),
                to: $('#to').val()
            });
    $('#stockReport').attr({target: '_blank', href: url});
}

function changeAccPeriod() {
    $('#to').attr({min: $('#from').val()});
    $('#to').attr({max: $('#from').attr('max')});
    if ($('#from').val() && $('#to').val()) {
        $('#accDetailButton').removeAttr("disabled");
    } else {
        $('#accDetailButton').attr("disabled", "disabled");
    }
}

function accountDetailAjax() {
    let today = new Date();
    let todayStr = today.toISOString().substring(0,10);
    let priorDate = new Date()
    priorDate.setDate(today.getDate()-30);
    let priorDateStr = priorDate.toISOString().substring(0,10);
    $.ajax({
        type: "GET",
        url: '/accountDetails?' + $.param({
            id: $('#id').val(),
            from: !$('#from').val() ? priorDateStr : $('#from').val(),
            to: !$('#to').val() ? todayStr : $('#to').val()
        }),
        success: function(data) {
            $('#paymentDetail').html(data);
            $(".convertTime").each(function() {
                let utcDate = $(this).text();
                let localDate = new Date(utcDate);
                let x = localDate.toLocaleDateString('en-GB', {
                    day: '2-digit', month: 'short', year: 'numeric'
                    }).replace(/ /g, '-');
                $(this).text(x);
            });
            $('.data-row').each(function() {
                let voucherType = $(this).find(".vType").text();
                if (voucherType == "PURCHASE" || voucherType == "RECEIPT") {
                    // $(this).addClass("table-danger");
                } else if (voucherType == "PAYMENT" || voucherType == "SALE") {
                    // $(this).addClass("table-success");
                }
                });
        },
        contentType: "application/json"
    });
}

$(document).ready(function() {
    $("#formPayment").hide();
    // Listen to click event on the submit button
    $('#button').click(function(e) {

        e.preventDefault();

        let vendor = {
            name: $("#name").val(),
            mobile: $("#mobile").val(),
            email: $("#email").val(),
            aadhaar: $("#aadhaar").val(),
            openingBalance: $("#openingBalance").val(),
            address1: $("#address1").val()
        };

        $.ajax({
            type: "POST",
            url: "/create",
            data: JSON.stringify(vendor),
            success: function(data) {
                $("#msg").show();
                $('#msg').html(data);
                $('html,body').animate({
                    scrollTop: $("#msg").offset().top
                }, 'slow');
                $('.clearit').val('');
                setTimeout(function() {
                    $("#msg").hide();
                }, 3000);
            },
            contentType: "application/json"
        });
    });

    $('#whButton').click(function(e) {

        e.preventDefault();

        let warehouse = {
            name: $("#whName").val(),
            location: $("#location").val()
        };

        $.ajax({
            type: "POST",
            url: "/createWarehouse",
            data: JSON.stringify(warehouse),
            success: function(data) {
                $("#msg").show();
                $('#msg').html(data);
                $('html,body').animate({
                    scrollTop: $("#msg").offset().top
                }, 'slow');
                $('.clearit').val('');
                setTimeout(function() {
                    $("#msg").hide();
                }, 3000);
            },
            contentType: "application/json"
        });
    });

    $('#pdButton').click(function(e) {

        e.preventDefault();
        let openingStocks = [];
        $('.stock').each(function(i, item) {
            let whName = $(item).attr('id').substring(5);
            let stockVal = $(item).val();
            let stock = {
                wareHouseName: whName,
                openingStock: stockVal
            };
            openingStocks.push(stock);
        })
        let product = {
            name: $("#pdName").val(),
            manufacturer: $("#manufacturer").val(),
            openingStocks: openingStocks
        };

        $.ajax({
            type: "POST",
            url: "/createProduct",
            data: JSON.stringify(product),
            success: function(data) {
                $("#msg").show();
                $('#msg').html(data);
                $('html,body').animate({
                    scrollTop: $("#msg").offset().top
                }, 'slow');
                $('.clearit').val('');
                setTimeout(function() {
                    $("#msg").hide();
                }, 3000);
            },
            contentType: "application/json"
        });
    });

    $('#idSearch').click(function(e) {
        e.preventDefault();
        accountDetailAjax();        
    });
    
    $('#productIdSearch').click(function(e) {
        console.log("here");
        e.preventDefault();
        $.ajax({
            type: "GET",
            url: '/stockDetails?' + $.param({
                id: $('#id').val()
            }),
            success: function(data) {
                $('#stockDetail').html(data);
                $(".convertTime").each(function() {
                    let utcDate = $(this).text();
                    let localDate = new Date(utcDate);
                    let x = localDate.toLocaleDateString('en-GB', {
                        day: '2-digit', month: 'short', year: 'numeric'
                      }).replace(/ /g, '-');
                    $(this).text(x);
                });
                $('.data-row').each(function() {
                    let voucherType = $(this).find(".vType").text();
                    if (voucherType == "PURCHASE" || voucherType == "RECEIPT") {
                        // $(this).addClass("table-danger");
                    } else if (voucherType == "PAYMENT" || voucherType == "SALE") {
                        // $(this).addClass("table-success");
                    }
                 });
            },
            contentType: "application/json"
        });
    });

    $("#studentSearch").on("keyup", function() {
        let value = $(this).val().toLowerCase();
        $("#studentTable tr").filter(function() {
          $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
        });
    });

    $("#product").on("keyup", function() {
        let value = $(this).val().toLowerCase();
        $(".product-value").filter(function() {
          $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
        });
    });
    
    $("body").on('keyup', ".price", function() {
        $('#value').attr('value', function() {
            let quantity = $('#quantity').val()
            let rate = $('#rate').val();
            return quantity * rate;
        });
    });

    $("body").on('click', "#submitVoucher", function() {
        let voucher = new Object();
        console.log($('#voucherId').val());
        if($('#voucherId').val() != '')
            return;
        voucher.voucherType = $("#voucherType").val();
        voucher.transactionDate = $("#transactionDate").val();
        if (voucher.voucherType == "PURCHASE" || voucher.voucherType == "SALE") {
            voucher.productId = $("#product").val();
            voucher.warehouseId = $("#warehouse").val();
            voucher.unit = $("#unit").val();
            voucher.quantity = $("#quantity").val();
            voucher.transactionInfo = $("#transactionInfo").val();
            voucher.rate = $("#rate").val();
            voucher.value = $("#value").val();
        } else if (voucher.voucherType == "PAYMENT" || voucher.voucherType == "RECEIPT") {
            voucher.mode = $("#mode").val();
            voucher.transactionInfo = $("#transactionId").val();
            voucher.value = $("#amount").val();
        }
        $.ajax({
            type: "POST",
            url: "/createVoucher?" + $.param({
                id: $('#id').val()
            }),
            data: JSON.stringify(voucher),
            success: function(data) {
                $("#msg").show();
                $('#msg').html(data);
                $('.clearit').val('');
                setTimeout(function() {
                    $("#msg").hide();
                    $('#exampleModalCenter').modal('hide');
                    $('.modal-backdrop').remove();
                    $("#idSearch").click();
                }, 3000);
            },
            contentType: "application/json"
        });
    });
    
    $("body").on('show.bs.modal', "#exampleModalCenter", function(event) {
        // Note: modal converts transactionInfo --> transactioninfo (all lower case)
        $('#msg').hide();
        setFstDropdown();
        let today = new Date();
        let todayStr = today.toISOString().substring(0,10);
        $('#transactionDate').attr({max: todayStr});


        let trig = $(event.relatedTarget);

        
        $('#exampleModalLongTitle').text(trig.data('action'));
        if(trig.data('action') != 'Edit Voucher') {
            return;
        }

        $('#voucherType').val(trig.data('vouchertype'));
        $('#voucherType').trigger("change");

        $('.product .fstlist').find('div[data-value="001"]').removeClass("selected");
        let prodSelector = $('.product .fstlist').find('div[data-value="'+trig.data('id')+'"]');
        prodSelector.addClass("selected");
        $('.product .fstselected').text(prodSelector.text());

        $('.warehouse .fstlist').find('div[data-value="001"]').removeClass("selected");
        let whSelector = $('.warehouse .fstlist').find('div[data-value="'+trig.data('warehouse')+'"]');
        whSelector.addClass("selected");
        $('.warehouse .fstselected').text(whSelector.text());
        
        setFstDropdown();
        $('#transactionDate').val(trig.data('transactiondate').substring(0,10));
        $('#transactionInfo').val(trig.data('transactioninfo'));
        $('#quantity').val(trig.data('quantity'));
        $('#rate').val(trig.data('rate'));
        $('#value').val(trig.data('value'));
        $('#amount').val(trig.data('value'));
        $('#mode').val(trig.data('mode'));
        $('#voucherId').text(trig.data('voucherid'));
    });
        
});