

// Commands
#define COMMAND_INSTRUCTION_LENGTH 4
#define TEAM_1_SCORE "T1SC"
#define TEAM_1_COLOUR "T1CL"
#define TEAM_2_SCORE "T2SC"
#define TEAM_2_COLOUR "T2CL"
#define REQUEST_INFO "INFO"
#define START_MARKER '<'
#define END_MARKER '>'

#define MAX_COMMAND_SIZE 10

class ScoreDisplayBTSync
{
    Stream &m_btSerial;
    Stream &m_logSerial;

    int m_team1Score = 0;
    int m_team1Red = 0xFF;
    int m_team1Green = 0x00;
    int m_team1Blue = 0x00;
    int m_team2Score = 0;
    int m_team2Red = 0xFF;
    int m_team2Green = 0x00;
    int m_team2Blue = 0x00;

    char m_commandBuffer[MAX_COMMAND_SIZE + 1]; // 1 extra character for the terminating 0
    int m_commandBufferPos = 0;
    bool m_commandReceiveStarted = false;

public:
    ScoreDisplayBTSync(Stream &btSerial, Stream &logSerial)
        : m_btSerial(btSerial), m_logSerial(logSerial)
    {
    }

    void update()
    {
        while (m_btSerial.available())
        {
            char receivedChar = m_btSerial.read();

            if (m_commandReceiveStarted)
            {
                if (receivedChar == END_MARKER)
                {
                    m_commandBuffer[m_commandBufferPos] = '\0';
                    String command(m_commandBuffer);
                    processCommand(command);

                    // Reset the command buffer
                    m_commandBufferPos = 0;
                    m_commandReceiveStarted = false;
                }
                else if (m_commandBufferPos >= MAX_COMMAND_SIZE)
                {
                    m_logSerial.println("Received command is too long. Discarding.");

                    // Reset the command buffer
                    m_commandBufferPos = 0;
                    m_commandReceiveStarted = false;
                }
                else
                {
                    m_commandBuffer[m_commandBufferPos++] = receivedChar;
                }
            }
            else if (receivedChar == START_MARKER)
            {
                m_commandReceiveStarted = true;
            }
            else
            {
                // we are not receiving a command nor starting one, so discard the character
            }
        }
    }

    int getTeam1Score()
    {
        return m_team1Score;
    }

    void getTeam1Color(int& red, int& green, int& blue)
    {
        red = m_team1Red;
        green = m_team1Green;
        blue = m_team1Blue;
    }

    int getTeam2Score()
    {
        return m_team2Score;
    }

    void getTeam2Color(int &red, int &green, int &blue)
    {
        red = m_team2Red;
        green = m_team2Green;
        blue = m_team2Blue;
    }

private:
    int strToHex(const String &str)
    {
        return (int)strtol(str.c_str(), nullptr, 16);
    }

    void processCommand(const String &command)
    {
        if (command.length() < COMMAND_INSTRUCTION_LENGTH)
        {
            m_logSerial.print("Invalid command: ");
            m_logSerial.println(command);
        }
        else
        {
            String instruction = command.substring(0, 4);
            String payload = command.substring(4, command.length());

            if (instruction == TEAM_1_SCORE)
            {
                if (payload.length() != 2)
                {
                    m_logSerial.print("Incorrect payload size for command ");
                    m_logSerial.print(TEAM_1_SCORE);
                    m_logSerial.println(". Expected 2 bytes.");
                }
                else
                {
                    m_team1Score = strToHex(payload);
                    m_logSerial.print("Team 1 score changed to ");
                    m_logSerial.println(m_team1Score);
                }
            }
            else if (instruction == TEAM_2_SCORE)
            {
                if (payload.length() != 2)
                {
                    m_logSerial.print("Incorrect payload size for command ");
                    m_logSerial.print(TEAM_2_SCORE);
                    m_logSerial.println(". Expected 2 bytes.");
                }
                else
                {
                    m_team2Score = strToHex(payload);
                    m_logSerial.print("Team 2 score changed to ");
                    m_logSerial.println(m_team2Score);
                }
            }
            else if (instruction == TEAM_1_COLOUR)
            {
                if (payload.length() != 6)
                {
                    m_logSerial.print("Incorrect payload size for command ");
                    m_logSerial.print(TEAM_1_COLOUR);
                    m_logSerial.println(". Expected 6 bytes.");
                }
                else
                {
                    m_team1Red = strToHex(payload.substring(0, 2));
                    m_team1Green = strToHex(payload.substring(2, 4));
                    m_team1Blue = strToHex(payload.substring(4, 6));
                    m_logSerial.print("Team 1 color changed to RGB ");
                    m_logSerial.print(m_team1Red);
                    m_logSerial.print(" ");
                    m_logSerial.print(m_team1Green);
                    m_logSerial.print(" ");
                    m_logSerial.print(m_team1Blue);
                    m_logSerial.println("");
                }
            }
            else if (instruction == TEAM_2_COLOUR)
            {
                if (payload.length() != 6)
                {
                    m_logSerial.print("Incorrect payload size for command ");
                    m_logSerial.print(TEAM_2_COLOUR);
                    m_logSerial.println(". Expected 6 bytes.");
                }
                else
                {
                    m_team2Red = strToHex(payload.substring(0, 2));
                    m_team2Green = strToHex(payload.substring(2, 4));
                    m_team2Blue = strToHex(payload.substring(4, 6));
                    m_logSerial.print("Team 2 color changed to RGB ");
                    m_logSerial.print(m_team2Red);
                    m_logSerial.print(" ");
                    m_logSerial.print(m_team2Green);
                    m_logSerial.print(" ");
                    m_logSerial.print(m_team2Blue);
                    m_logSerial.println("");
                }
            }
            else if (instruction == REQUEST_INFO)
            {
                char team1ScoreCommand[16];
                sprintf(team1ScoreCommand, "<T1SC%02X>", m_team1Score);
                char team2ScoreCommand[16];
                sprintf(team2ScoreCommand, "<T2SC%02X>", m_team2Score);
                char team1ColorCommand[16];
                sprintf(team1ColorCommand, "<T1CL%02X%02X%02X>", m_team1Red, m_team1Green, m_team1Blue);
                char team2ColorCommand[16];
                sprintf(team2ColorCommand, "<T1CL%02X%02X%02X>", m_team2Red, m_team2Green, m_team2Blue);

                m_btSerial.println(team1ScoreCommand);
                m_btSerial.println(team2ScoreCommand);
                m_btSerial.println(team1ColorCommand);
                m_btSerial.println(team2ColorCommand);
            }
        }
    }
};
