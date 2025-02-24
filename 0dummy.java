import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.Base64;

@RestController
@AssignmentHints({"insecure-deserialization.hints.1", "insecure-deserialization.hints.2", "insecure-deserialization.hints.3"})
public class InsecureDeserializationTask extends AssignmentEndpoint {

	String PASSWORD = "!!webgoat_admin_1234!!";
    String PASSWORD_TOM = "thisisasecretfortomonly";
    String ADMIN_PASSWORD_LINK = "375afe1104f4a487a73823c50a9292a2";


    @PostMapping("/InsecureDeserialization/task")
    @ResponseBody
    public AttackResult completed(@RequestParam String token) throws IOException {
        String b64token;
        long before;
        long after;
        int delay;

        b64token = token.replace('-', '+').replace('_', '/');

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(b64token)))) {
            before = System.currentTimeMillis();
            Object o = ois.readObject();
            if (!(o instanceof VulnerableTaskHolder)) {
                if (o instanceof String) {
                    return failed(this).feedback("insecure-deserialization.stringobject").build();
                }
                return failed(this).feedback("insecure-deserialization.wrongobject").build();
            }
            after = System.currentTimeMillis();
        } catch (InvalidClassException e) {
            return failed(this).feedback("insecure-deserialization.invalidversion").build();
        } catch (IllegalArgumentException e) {
            return failed(this).feedback("insecure-deserialization.expired").build();
        } catch (Exception e) {
            return failed(this).feedback("insecure-deserialization.invalidversion").build();
        }

        delay = (int) (after - before);
        if (delay > 7000) {
            return failed(this).build();
        }
        if (delay < 3000) {
            return failed(this).build();
        }
        return success(this).build();
    }
}
