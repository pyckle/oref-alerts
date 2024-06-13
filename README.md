# oref-alerts

An Unofficial Java Library and Swing UI to programmically access and display Alerts from Israel's Homefront Command

## What is the purpose of this project?

* A Java library that reliably receives alerts from Pekudei Oref's unofficial REST APIs
    - Programmatically merge output of multiple Oref REST APIs
    - Automatically update information asynchronously to program execution
* A SwingUI that displays recent historical alerts and regularly updates with new alerts
* Lightweight - must run on a Raspberry Pi 3A+ (512 MB RAM) and render at 1080p

## Features

* Rich library with clean API to integration with Pekudei Oref's unofficial APIs
    - Updates from Unofficial Pekudei Oref API regularly
    - Refreshes historical API in the case of a network outage to prevent missed Alerts
    - Fetches city and alert translations from Oref APIs
    - Respects HTTP Caching Headers and adds in random delay to avoid server load after cache expiration
    - Decodes alert time from Alert API id (which appears to use Microsoft's Filetime Epoch). Graceful fallback on
      failure

* SwingUI to display alerts
    - Designed to work well on wide screens, not narrow phones
    - Autoresize number of alerts displayed based on window size
    - Shows last updated time from last-modified HTTP Response Header (rather than local or server timestamp)
    - Supports Hebrew, English, Russian, and Arabic
    - Minimalistic
        - Very few dependencies
        - JAR is <.5MB
        - Runs fine with 32 MB Java Heap

## TODO

* More automated tests are necessary
* Support fullscreen
* Support multiple monitors
* Add more visible warning:
  * If updates fail to fetch
  * On specific area(s)
* Better documentation

## Build Instructions

### Requirements

* Java 17+
* A recent version of Maven
* Access to Pekudei Oref (limited to Israel IPs)

### Build Instructions

Checkout this repository<br />
run <pre>mvn clean install</pre>
A self executable jar will be built: <pre>./oref-swingui/target/oref-swingui.jar</pre>
Run it!
<pre>java -jar ./oref-swingui/target/oref-swingui.jar</pre>
If you want to change the language (Hebrew by default)
<pre>java -jar ./oref-swingui/target/oref-swingui.jar en</pre>
or make the font bigger
<pre>java -jar ./oref-swingui/target/oref-swingui.jar 20 en</pre>

## Disclaimers

This application is not intended as a substitute to adhering to Homefront Command's guidelines.
It utilizes unofficial APIs and should **not** be relied upon for receiving live saving notifications.
**Always** follow official lifesaving instructions and procedures.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

