(function(){var n={3274:function(n,e,t){"use strict";var r=t(9242),o=t(3396);function i(n,e,t,r,i,u){const c=(0,o.up)("Player");return(0,o.wg)(),(0,o.iD)("div",null,[(0,o.Wm)(c)])}var u=t(6520);const c=(0,o._)("video",{id:"myMovie",class:"video-js vjs-default-skin vjs-big-play-centered vjs-16-9"},[(0,o._)("source",{id:"source"})],-1),f=[c];function a(n,e,t,r,i,u){return(0,o.wg)(),(0,o.iD)("div",null,f)}var s=t(1383),l=t(2807),p=class extends u.w3{mounted(){location.search.substring(1).split("&").forEach((function(n){var e=n.split("=");if("target"==e[0]){let n=decodeURIComponent(e[1]),t="";if(n.endsWith(".mp4"))t="video/mp4";else{if(!n.endsWith(".m3u8"))return;t="application/x-mpegURL"}(0,s.Z)("myMovie",{controls:!0,autoplay:!0,preload:"auto",muted:!0,language:"zh-CN",fluid:!0,sources:[{src:n,type:t}]},(function(){this.on("error",(function(){l.z8.error("视频加载失败，请确认视频是否存在或您是否有访问权限")})),this.play()}))}}))}},v=t(89);const d=(0,v.Z)(p,[["render",a]]);var h=d,b=function(n,e,t,r){var o,i=arguments.length,u=i<3?e:null===r?r=Object.getOwnPropertyDescriptor(e,t):r;if("object"===typeof Reflect&&"function"===typeof Reflect.decorate)u=Reflect.decorate(n,e,t,r);else for(var c=n.length-1;c>=0;c--)(o=n[c])&&(u=(i<3?o(u):i>3?o(e,t,u):o(e,t))||u);return i>3&&u&&Object.defineProperty(e,t,u),u};let g=class extends u.w3{};g=b([(0,u.Ei)({components:{Player:h}})],g);var y=g;const m=(0,v.Z)(y,[["render",i]]);var w=m,O=t(5365);t(4415);const j=(0,r.ri)(w);j.use(O.Z),j.mount("#app"),j.config.globalProperties.$backend=""},5893:function(){},5586:function(){}},e={};function t(r){var o=e[r];if(void 0!==o)return o.exports;var i=e[r]={exports:{}};return n[r].call(i.exports,i,i.exports,t),i.exports}t.m=n,function(){var n=[];t.O=function(e,r,o,i){if(!r){var u=1/0;for(s=0;s<n.length;s++){r=n[s][0],o=n[s][1],i=n[s][2];for(var c=!0,f=0;f<r.length;f++)(!1&i||u>=i)&&Object.keys(t.O).every((function(n){return t.O[n](r[f])}))?r.splice(f--,1):(c=!1,i<u&&(u=i));if(c){n.splice(s--,1);var a=o();void 0!==a&&(e=a)}}return e}i=i||0;for(var s=n.length;s>0&&n[s-1][2]>i;s--)n[s]=n[s-1];n[s]=[r,o,i]}}(),function(){t.n=function(n){var e=n&&n.__esModule?function(){return n["default"]}:function(){return n};return t.d(e,{a:e}),e}}(),function(){t.d=function(n,e){for(var r in e)t.o(e,r)&&!t.o(n,r)&&Object.defineProperty(n,r,{enumerable:!0,get:e[r]})}}(),function(){t.g=function(){if("object"===typeof globalThis)return globalThis;try{return this||new Function("return this")()}catch(n){if("object"===typeof window)return window}}()}(),function(){t.o=function(n,e){return Object.prototype.hasOwnProperty.call(n,e)}}(),function(){var n={35:0};t.O.j=function(e){return 0===n[e]};var e=function(e,r){var o,i,u=r[0],c=r[1],f=r[2],a=0;if(u.some((function(e){return 0!==n[e]}))){for(o in c)t.o(c,o)&&(t.m[o]=c[o]);if(f)var s=f(t)}for(e&&e(r);a<u.length;a++)i=u[a],t.o(n,i)&&n[i]&&n[i][0](),n[i]=0;return t.O(s)},r=self["webpackChunkmovie_web"]=self["webpackChunkmovie_web"]||[];r.forEach(e.bind(null,0)),r.push=e.bind(null,r.push.bind(r))}();var r=t.O(void 0,[998],(function(){return t(3274)}));r=t.O(r)})();
//# sourceMappingURL=player.dfa969b0.js.map