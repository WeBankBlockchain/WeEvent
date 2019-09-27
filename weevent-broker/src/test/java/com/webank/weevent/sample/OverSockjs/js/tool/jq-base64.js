jQuery.base64 = ( function( $ ) {
   
  var _PADCHAR = "=",
    _ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
    _VERSION = "1.1";//Mr. Ruan fix to 1.1 to support asian char(utf8)
 
 
  function _getbyte64( s, i ) {
    // This is oddly fast, except on Chrome/V8.
    // Minimal or no improvement in performance by using a
    // object with properties mapping chars to value (eg. 'A': 0)
 
    var idx = _ALPHA.indexOf( s.charAt( i ) );
 
    if ( idx === -1 ) {
      throw "Cannot decode base64";
    }
 
    return idx;
  }
   
  function _decode_chars(y, x){
      while(y.length > 0){
        var ch = y[0];
        if(ch < 0x80) {
            y.shift();
            x.push(String.fromCharCode(ch));
        }else if((ch & 0x80) === 0xc0){
            if(y.length < 2) break;
            ch = y.shift();
            var ch1 = y.shift();
            x.push(String.fromCharCode( ((ch & 0x1f) << 6) + (ch1 & 0x3f)));
        }else{
            if(y.length < 3) break;
            ch = y.shift();
            var ch1 = y.shift();
            var ch2 = y.shift();
            x.push(String.fromCharCode(((ch & 0x0f) << 12) + ((ch1 & 0x3f) << 6) + (ch2 & 0x3f)));
        }    
      }
  }
   
  function _decode( s ) {
    var pads = 0,
      i,
      b10,
      imax = s.length,
      x = [],
      y = [];
 
    s = String( s );
     
    if ( imax === 0 ) {
      return s;
    }
 
    if ( imax % 4 !== 0 ) {
      throw "Cannot decode base64";
    }
 
    if ( s.charAt( imax - 1 ) === _PADCHAR ) {
      pads = 1;
 
      if ( s.charAt( imax - 2 ) === _PADCHAR ) {
        pads = 2;
      }
 
      // either way, we want to ignore this last block
      imax -= 4;
    }
 
    for ( i = 0; i < imax; i += 4 ) {
      var ch1 = _getbyte64( s, i );
      var ch2 = _getbyte64( s, i + 1);
      var ch3 = _getbyte64( s, i + 2);
      var ch4 = _getbyte64( s, i + 3);
       
      b10 = ( _getbyte64( s, i ) << 18 ) | ( _getbyte64( s, i + 1 ) << 12 ) | ( _getbyte64( s, i + 2 ) << 6 ) | _getbyte64( s, i + 3 );
      y.push(b10 >> 16);
      y.push((b10 >> 8) & 0xff);
      y.push(b10 & 0xff);
      _decode_chars(y, x);
    }
    switch ( pads ) {
      case 1:
        b10 = ( _getbyte64( s, i ) << 18 ) | ( _getbyte64( s, i + 1 ) << 12 ) | ( _getbyte64( s, i + 2 ) << 6 );
        y.push(b10 >> 16);
        y.push((b10 >> 8) & 0xff);
        break;
 
      case 2:
        b10 = ( _getbyte64( s, i ) << 18) | ( _getbyte64( s, i + 1 ) << 12 );
        y.push(b10 >> 16);
        break;
    }
    _decode_chars(y, x);
    if(y.length > 0) throw "Cannot decode base64";
    return x.join( "" );
  }
   
   
  function _get_chars(ch, y){
    if(ch < 0x80) y.push(ch);
    else if(ch < 0x800){
        y.push(0xc0 + ((ch >> 6) & 0x1f));
        y.push(0x80 + (ch & 0x3f));
    }else{
        y.push(0xe0 + ((ch >> 12) & 0xf));
        y.push(0x80 + ((ch >> 6) & 0x3f));
        y.push(0x80 + (ch & 0x3f));
    }
  }
   
   
   
  function _encode( s ) {
    if ( arguments.length !== 1 ) {
      throw "SyntaxError: exactly one argument required";
    }
 
    s = String( s );
    if ( s.length === 0 ) {
      return s;
    }
     
    //s = _encode_utf8(s);
    var i,
      b10,
      y = [],
      x = [],
      len = s.length;
    i = 0;
    while(i < len){
        _get_chars(s.charCodeAt(i), y);
        while(y.length >= 3){
            var ch1 = y.shift();
            var ch2 = y.shift();
            var ch3 = y.shift();
            b10 = ( ch1 << 16 ) | ( ch2 << 8 ) | ch3;
            x.push( _ALPHA.charAt( b10 >> 18 ) );
            x.push( _ALPHA.charAt( ( b10 >> 12 ) & 0x3F ) );
            x.push( _ALPHA.charAt( ( b10 >> 6 ) & 0x3f ) );
            x.push( _ALPHA.charAt( b10 & 0x3f ) );
        }
        i++;
    }
      
 
    switch ( y.length ) {
      case 1:
        var ch = y.shift();
        b10 = ch << 16;
        x.push( _ALPHA.charAt( b10 >> 18 ) + _ALPHA.charAt( ( b10 >> 12 ) & 0x3F ) + _PADCHAR + _PADCHAR );
        break;
 
      case 2:
        var ch1 = y.shift();
        var ch2 = y.shift();
        b10 = ( ch1 << 16 ) | ( ch2 << 8 );
        x.push( _ALPHA.charAt( b10 >> 18 ) + _ALPHA.charAt( ( b10 >> 12 ) & 0x3F ) + _ALPHA.charAt( ( b10 >> 6 ) & 0x3f ) + _PADCHAR );
        break;
    }
 
    return x.join( "" );
  }
 
 
  return {
    decode: _decode,
    encode: _encode,
    VERSION: _VERSION
  };
       

}( jQuery ) );