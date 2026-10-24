package cn.bugstack.ai.domain.agent.service;

import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import java.util.List;

/**
 * 从 ADK 事件中提取"最终回答"文本（排除思考内容）。
 *
 * <p>ADK 的流式事件是"累计快照"：每一帧都包含到目前为止的完整文本。
 * 所以要配合 {@link #delta(String, String)} 只取新增部分，避免重复拼接。</p>
 */
public final class AgentEventTextExtractor {

    private AgentEventTextExtractor() {
    }

    /** 取这一帧里的回答文本（不含 thought 片段）。 */
    public static String answerOf(Event event) {
        StringBuilder answer = new StringBuilder();
        event.content().flatMap(Content::parts).orElse(List.of()).forEach(part -> {
            if (part.text().isPresent() && !part.thought().orElse(false)) {
                answer.append(part.text().get());
            }
        });
        return answer.toString();
    }

    /** 从累计快照里取新增部分：新快照以旧快照开头就截掉旧的部分，否则视为全新内容。 */
    public static String delta(String previous, String current) {
        if (current == null || current.isEmpty()) {
            return "";
        }
        String safePrevious = previous == null ? "" : previous;
        if (current.startsWith(safePrevious)) {
            return current.substring(safePrevious.length());
        }
        return current;
    }
}
