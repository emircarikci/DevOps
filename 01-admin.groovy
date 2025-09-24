import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()
def user = System.getenv("JENKINS_ADMIN_ID") ?: "admin"
def pass = System.getenv("JENKINS_ADMIN_PASSWORD") ?: "admin"

if (instance.getSecurityRealm() instanceof HudsonPrivateSecurityRealm == false) {
  instance.setSecurityRealm(new HudsonPrivateSecurityRealm(false))
}
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)
if (instance.getUser(user) == null) {
  def u = instance.getSecurityRealm().createAccount(user, pass)
  u.save()
}
instance.save()
println "Jenkins admin user ensured: ${user}"
