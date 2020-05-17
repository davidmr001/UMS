/**
 * 
 */
package top.dcenter.security.social.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zhailiang
 *
 */
@Component("connect/status")
public class ConnectionStatusView extends AbstractView {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		Map<String, List<Connection<?>>> connections = (Map<String, List<Connection<?>>>) model.get("connectionMap");
		
		Map<String, Boolean> result = new HashMap<>();
		Iterator<Map.Entry<String, List<Connection<?>>>> iterator = connections.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<String, List<Connection<?>>> next = iterator.next();
			result.put(next.getKey(), CollectionUtils.isNotEmpty(next.getValue()));
		}

		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(result));
	}

}
