package com.daibutsu;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

// サーブレットフィルタ
@WebFilter(filterName = "CorsFilter", urlPatterns = { "/*" })
public class CorsFilter implements Filter {
  @Override
  public void init(FilterConfig config) {
  }

  // フィルター処理
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      // 以下の通りCORS対応のためHeaderを指定する
      HttpServletResponse http = (HttpServletResponse) response;
      http.addHeader("Access-Control-Allow-Origin", "*");
      http.addHeader("Access-Control-Allow-Headers", "*");
      http.addHeader("Access-Control-Allow-Credentials", "true");
      http.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS");
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}